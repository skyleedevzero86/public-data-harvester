package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.client.CorpApiClient;
import com.antock.api.coseller.application.client.RegionApiClient;
import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
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

/**
 * 법인 판매 데이터 가공을 위한 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoSellerService {

    // csv 조회 서비스
    private final CsvService csvService;
    // 법인 등록번호 조회 api 호출
    private final CorpApiClient corpApiClient;
    // 행정 구역 코드 조회 api 호출
    private final RegionApiClient regionApiClient;
    // Repository
    private final CorpMastStore corpMastStore;

    /**
     * 법인 데이터 저장 로직
     * @param requestDto 지역 요청 DTO (시/도, 구/군 포함)
     * @return 저장된 법인 마스터 데이터의 총 개수
     */
    @Transactional // 전체 작업에 대한 트랜잭션 적용 (CSV 읽기, API 호출, DB 저장)
    public int saveCoSeller(RegionRequestDto requestDto) {
        log.info("데이터 저장 프로세스 시작: City={}, District={}", requestDto.getCity().name(), requestDto.getDistrict().name());

        // City와 district로 csv 파일 읽어오기
        List<BizCsvInfoDto> csvList = csvService.readBizCsv(requestDto.getCity().name(), requestDto.getDistrict().name());
        log.info("CSV 파일에서 읽어온 유효한 데이터 수: {}건", csvList.size());

        if (csvList.isEmpty()) {
            log.warn("CSV 파일에서 읽어올 유효한 데이터가 없습니다. 저장 작업을 종료합니다.");
            return 0;
        }

        // 받아온 csv로 API 호출하여 법인 등록 코드, 행정 구역 코드 받아오기
        List<CorpMastCreateDTO> corpCreateDtoList = getCorpApiInfo(csvList);
        log.info("API 호출을 통해 가공된 데이터 수: {}건", corpCreateDtoList.size());

        if (corpCreateDtoList.isEmpty()) {
            log.warn("API 호출 후 생성된 데이터가 없습니다. 저장 작업을 종료합니다.");
            return 0;
        }

        // 저장
        int savedCnt = saveCorpMastList(corpCreateDtoList);
        log.info("최종 데이터베이스 저장 완료: 총 {}건 저장됨.", savedCnt);
        return savedCnt;
    }

    /**
     * DTO 리스트를 CorpMast 엔티티로 변환하여 저장합니다.
     * @param corpCreateDtoList 저장할 CorpMastCreateDTO 리스트
     * @return 실제로 저장된 엔티티의 개수
     */
    private int saveCorpMastList(List<CorpMastCreateDTO> corpCreateDtoList) {
        List<CorpMast> entitiesToSave = corpCreateDtoList.stream()
                .map(CorpMastCreateDTO::toEntity)
                .collect(Collectors.toList());

        int savedCount = 0;
        List<String> duplicatedBizNos = new ArrayList<>(); // 중복되어 저장되지 않은 bizNo 목록

        log.info("데이터베이스 저장 시작 - 총 {}건", entitiesToSave.size());

        // 대량 저장을 위한 반복문
        for (CorpMast entity : entitiesToSave) {
            try {
                // bizNo를 기준으로 이미 존재하는지 확인
                Optional<CorpMast> existingEntity = corpMastStore.findByBizNo(entity.getBizNo());

                if (existingEntity.isPresent()) {
                    // 이미 존재하는 경우 업데이트 로직 (필요시)
                    // 여기서는 단순히 스킵하고 중복으로 처리합니다.
                    log.debug("이미 존재하는 bizNo로 인해 저장 스킵: {}", entity.getBizNo());
                    duplicatedBizNos.add(entity.getBizNo());
                } else {
                    // 새 엔티티 저장
                    corpMastStore.save(entity);
                    savedCount++;
                }
            } catch (DataIntegrityViolationException ex) {
                // Unique Constraint 위반 (bizNo 중복)으로 인한 예외 처리
                log.warn("데이터베이스 저장 실패 (bizNo 중복): bizNo={}, 오류: {}", entity.getBizNo(), ex.getMessage());
                duplicatedBizNos.add(entity.getBizNo());
            } catch (Exception ex) {
                // 그 외 저장 중 발생한 다른 예외 처리
                log.error("데이터베이스 저장 중 예상치 못한 오류 발생: bizNo={}, 오류: {}", entity.getBizNo(), ex.getMessage(), ex);
                duplicatedBizNos.add(entity.getBizNo());
            }
        }

        if (!duplicatedBizNos.isEmpty()) {
            log.info("데이터베이스에 저장되지 못한 건수 (중복 또는 오류): {}건", duplicatedBizNos.size());
            // 디버그 레벨에서 중복된 bizNo 목록 로깅 (운영 환경에서는 과도한 로깅이 될 수 있음)
            log.debug("저장 실패한 bizNo 목록: {}", duplicatedBizNos);
        }
        return savedCount;
    }

    /**
     * CSV 정보 리스트로부터 법인 등록 코드와 행정 구역 코드를 비동기적으로 조회하여 DTO 리스트를 반환합니다.
     * @param csvList BizCsvInfoDto 리스트
     * @return API 호출을 통해 정보가 보강된 CorpMastCreateDTO 리스트
     */
    private List<CorpMastCreateDTO> getCorpApiInfo(List<BizCsvInfoDto> csvList) {
        // CompletableFuture를 사용하여 각 CSV 정보에 대해 비동기적으로 API 호출을 수행
        List<CompletableFuture<Optional<CorpMastCreateDTO>>> futures = csvList.stream()
                .map(this::processAsync) // 각 BizCsvInfoDto를 비동기 처리
                .collect(Collectors.toList());

        // 모든 CompletableFuture가 완료될 때까지 대기하고 결과를 수집
        return futures.stream()
                .map(CompletableFuture::join) // CompletableFuture의 결과를 가져옴 (블로킹)
                .filter(Optional::isPresent) // Optional이 비어있지 않은 결과만 필터링
                .map(Optional::get) // Optional에서 실제 값 추출
                .collect(Collectors.toList());
    }

    /**
     * 단일 BizCsvInfoDto에 대해 법인 등록 번호와 지역 코드를 비동기적으로 조회합니다.
     * @param csvInfo 처리할 BizCsvInfoDto
     * @return CorpMastCreateDTO를 포함하는 CompletableFuture
     */
    @Async
    public CompletableFuture<Optional<CorpMastCreateDTO>> processAsync(BizCsvInfoDto csvInfo) {
        // 법인 등록 번호 API 호출 비동기 처리
        CompletableFuture<String> corpFuture = corpApiClient.getCorpRegNo(csvInfo.getBizNo());
        // 행정 구역 코드 API 호출 비동기 처리
        CompletableFuture<String> regionFuture = regionApiClient.getRegionCode(csvInfo.getBizAddress());

        // 두 비동기 작업이 모두 완료되면 결과를 조합하여 CorpMastCreateDTO 생성
        return corpFuture.thenCombine(regionFuture, (corpRegNo, regionCd) -> {
            if (corpRegNo == null && regionCd == null) {
                // 두 API 호출 모두 실패한 경우 (또는 유효하지 않은 응답)
                log.debug("API 호출 실패: bizNo={}, bizNm={}. 빈 값으로 DTO 생성합니다.", csvInfo.getBizNo(), csvInfo.getBizNm());
                // 모든 API 호출이 실패했더라도 DTO는 생성하되, 값은 빈 문자열로 설정하여 데이터 누락 방지
                return Optional.of(
                        CorpMastCreateDTO.builder()
                                .sellerId(csvInfo.getSellerId())
                                .bizNm(csvInfo.getBizNm())
                                .bizNo(csvInfo.getBizNo())
                                .corpRegNo("")
                                .regionCd("")
                                .build()
                );
            }

            return Optional.of(
                    CorpMastCreateDTO.builder()
                            .sellerId(csvInfo.getSellerId())
                            .bizNm(csvInfo.getBizNm())
                            .bizNo(csvInfo.getBizNo())
                            .corpRegNo(Optional.ofNullable(corpRegNo).orElse(""))
                            .regionCd(Optional.ofNullable(regionCd).orElse(""))
                            .build()
            );
        }).exceptionally(ex -> {

            log.error("비동기 API 처리 중 예외 발생: bizNo={}, bizNm={}. 오류: {}", csvInfo.getBizNo(), csvInfo.getBizNm(), ex.getMessage(), ex);
            return Optional.empty(); // 이 데이터는 최종 목록에서 제외됩니다.
        });
    }
}