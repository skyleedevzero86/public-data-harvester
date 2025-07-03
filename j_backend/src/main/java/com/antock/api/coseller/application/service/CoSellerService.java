package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.client.CorpApiClient;
import com.antock.api.coseller.application.client.RegionApiClient;
import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoSellerService {

    private final CsvService csvService;
    private final CorpApiClient corpApiClient;
    private final RegionApiClient regionApiClient;
    private final CorpMastStore corpMastStore;

    @Transactional
    public int saveCoSeller(RegionRequestDto requestDto) {
        log.info("데이터 저장 프로세스 시작: City={}, District={}", requestDto.getCity().name(), requestDto.getDistrict().name());

        List<BizCsvInfoDto> csvList = csvService.readBizCsv(requestDto.getCity().name(),
                requestDto.getDistrict().name());
        log.info("CSV 파일에서 읽어온 유효한 데이터 수: {}건", csvList.size());

        if (csvList.isEmpty()) {
            log.warn("CSV 파일에서 읽어올 유효한 데이터가 없습니다. 저장 작업을 종료합니다.");
            return 0;
        }

        List<CorpMastCreateDTO> corpCreateDtoList = getCorpApiInfo(csvList);
        log.info("API 호출을 통해 가공된 데이터 수: {}건", corpCreateDtoList.size());

        if (corpCreateDtoList.isEmpty()) {
            log.warn("API 호출 후 생성된 데이터가 없습니다. 저장 작업을 종료합니다.");
            return 0;
        }

        int savedCnt = saveCorpMastList(corpCreateDtoList);
        log.info("최종 데이터베이스 저장 완료: 총 {}건 저장됨.", savedCnt);
        return savedCnt;
    }

    private int saveCorpMastList(List<CorpMastCreateDTO> corpCreateDtoList) {
        List<CorpMast> entitiesToSave = corpCreateDtoList.stream()
                .map(CorpMastCreateDTO::toEntity)
                .collect(Collectors.toList());

        int savedCount = 0;
        List<String> duplicatedBizNos = new ArrayList<>();

        log.info("데이터베이스 저장 시작 - 총 {}건", entitiesToSave.size());

        for (CorpMast entity : entitiesToSave) {
            try {
                Optional<CorpMast> existingEntity = corpMastStore.findByBizNo(entity.getBizNo());

                if (existingEntity.isPresent()) {
                    log.debug("이미 존재하는 bizNo로 인해 저장 스킵: {}", entity.getBizNo());
                    duplicatedBizNos.add(entity.getBizNo());
                } else {
                    corpMastStore.save(entity);
                    savedCount++;
                }
            } catch (DataIntegrityViolationException ex) {
                log.warn("데이터베이스 저장 실패 (bizNo 중복): bizNo={}, 오류: {}", entity.getBizNo(), ex.getMessage());
                duplicatedBizNos.add(entity.getBizNo());
            } catch (Exception ex) {
                log.error("데이터베이스 저장 중 예상치 못한 오류 발생: bizNo={}, 오류: {}", entity.getBizNo(), ex.getMessage(), ex);
                duplicatedBizNos.add(entity.getBizNo());
            }
        }

        if (!duplicatedBizNos.isEmpty()) {
            log.info("데이터베이스에 저장되지 못한 건수 (중복 또는 오류): {}건", duplicatedBizNos.size());
            log.debug("저장 실패한 bizNo 목록: {}", duplicatedBizNos);
        }
        return savedCount;
    }

    private List<CorpMastCreateDTO> getCorpApiInfo(List<BizCsvInfoDto> csvList) {

        List<CompletableFuture<Optional<CorpMastCreateDTO>>> futures = csvList.stream()
                .map(this::processAsync)
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Async
    public CompletableFuture<Optional<CorpMastCreateDTO>> processAsync(BizCsvInfoDto csvInfo) {

        log.info("=== processAsync 시작 ===");
        log.info("입력 데이터 - bizNo: {}, bizNm: {}, bizAddress: {}",
                csvInfo.getBizNo(), csvInfo.getBizNm(), csvInfo.getBizAddress());

        CompletableFuture<String> corpFuture = corpApiClient.getCorpRegNo(csvInfo.getBizNo());
        CompletableFuture<RegionInfoDto> regionFuture = regionApiClient.getRegionInfo(csvInfo.getBizAddress());
        return corpFuture.thenCombine(regionFuture, (corpRegNo, regionInfo) -> {
            log.debug("법인등록번호 API 결과: bizNo={}, corpRegNo={}", csvInfo.getBizNo(), corpRegNo);
            log.debug("행정구역 API 결과: address={}, regionInfo={}", csvInfo.getBizAddress(), regionInfo);

            log.info("API 호출 결과 - bizNo: {}, corpRegNo: '{}', regionInfo: {}",
                    csvInfo.getBizNo(), corpRegNo, regionInfo);

            if (corpRegNo == null && regionInfo == null) {
                log.debug("API 호출 실패: bizNo={}, bizNm={}. 빈 값으로 DTO 생성합니다.", csvInfo.getBizNo(), csvInfo.getBizNm());
                return Optional.of(
                        CorpMastCreateDTO.builder()
                                .sellerId(csvInfo.getSellerId())
                                .bizNm(csvInfo.getBizNm())
                                .bizNo(csvInfo.getBizNo())
                                .corpRegNo("")
                                .regionCd("")
                                .siNm("")
                                .sggNm("")
                                .build());
            }
            log.info("최종 DTO 생성 - corpRegNo: '{}'", Optional.ofNullable(corpRegNo).orElse(""));

            return Optional.of(
                    CorpMastCreateDTO.builder()
                            .sellerId(csvInfo.getSellerId())
                            .bizNm(csvInfo.getBizNm())
                            .bizNo(csvInfo.getBizNo())
                            //.regionCd(regionInfo != null ? regionInfo.getCorpRegNo() : "")
                            .corpRegNo(Optional.ofNullable(corpRegNo).orElse(""))
                            .regionCd(regionInfo != null ? regionInfo.getRegionCd() : "")
                            .siNm(regionInfo != null ? regionInfo.getSiNm() : "")
                            .sggNm(regionInfo != null ? regionInfo.getSggNm() : "")
                            .build());
        }).exceptionally(ex -> {
            log.error("비동기 API 처리 중 예외 발생: bizNo={}, bizNm={}. 오류: {}", csvInfo.getBizNo(), csvInfo.getBizNm(),
                    ex.getMessage(), ex);
            return Optional.empty();
        });
    }
}