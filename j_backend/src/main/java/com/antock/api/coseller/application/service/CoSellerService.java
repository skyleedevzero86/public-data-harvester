package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.client.CorpApiClient;
import com.antock.api.coseller.application.client.RegionApiClient;
import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.domain.CorpMastHistory;
import com.antock.api.coseller.infrastructure.CorpMastStore;
import com.antock.api.coseller.infrastructure.CorpMastHistoryStore;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoSellerService {

    private static final String ACTION_INSERT = "INSERT";
    private static final String RESULT_SUCCESS = "SUCCESS";
    private static final String RESULT_DUPLICATE = "DUPLICATE";
    private static final String RESULT_FAIL = "FAIL";

    private final CsvService csvService;
    private final CorpApiClient corpApiClient;
    private final RegionApiClient regionApiClient;
    private final CorpMastStore corpMastStore;
    private final CorpMastHistoryStore corpMastHistoryStore;

    @Value("${batch.processing.chunk-size:100}")
    private int chunkSize;

    @Value("${batch.processing.max-concurrent:10}")
    private int maxConcurrent;

    @Value("${batch.processing.retry-attempts:3}")
    private int retryAttempts;

    @Transactional
    public int saveCoSeller(RegionRequestDto requestDto, String username) {
        if (requestDto == null || !StringUtils.hasText(username)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "필수 파라미터가 누락되었습니다.");
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("전체 처리");

        log.info("데이터 저장 프로세스 시작: City={}, District={}, username={}",
                requestDto.getCity().name(), requestDto.getDistrict().name(), username);

        try {
            List<BizCsvInfoDto> csvList = readCsvData(requestDto, stopWatch);
            if (csvList.isEmpty()) {
                return 0;
            }

            List<CorpMastCreateDTO> corpCreateDtoList = processApiCalls(csvList, username, stopWatch);
            if (corpCreateDtoList.isEmpty()) {
                return 0;
            }

            int savedCnt = saveToDatabaseOptimized(corpCreateDtoList, username, stopWatch);

            log.info("최종 데이터베이스 저장 완료: 총 {}건 저장됨.", savedCnt);
            return savedCnt;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("데이터 저장 프로세스 중 예상치 못한 오류 발생", e);
            throw new BusinessException(ErrorCode.CORP_SEARCH_ERROR,
                    "데이터 저장 프로세스 실패: " + e.getMessage());
        } finally {
            stopWatch.stop();
            log.info("처리 시간 요약:\n{}", stopWatch.prettyPrint());
        }
    }

    private List<BizCsvInfoDto> readCsvData(RegionRequestDto requestDto, StopWatch stopWatch) {
        stopWatch.start("CSV 파일 읽기");
        try {
            List<BizCsvInfoDto> csvList = csvService.readBizCsv(
                    requestDto.getCity().name(), requestDto.getDistrict().name());

            log.info("CSV 파일에서 읽어온 유효한 데이터 수: {}건", csvList.size());

            if (csvList.isEmpty()) {
                log.warn("CSV 파일에서 읽어올 유효한 데이터가 없습니다. 저장 작업을 종료합니다.");
            }

            return csvList;
        } finally {
            stopWatch.stop();
        }
    }

    private List<CorpMastCreateDTO> processApiCalls(List<BizCsvInfoDto> csvList, String username, StopWatch stopWatch) {
        stopWatch.start("API 호출 및 데이터 가공");
        try {
            List<CorpMastCreateDTO> corpCreateDtoList = getCorpApiInfoOptimized(csvList, username);

            log.info("API 호출을 통해 가공된 데이터 수: {}건", corpCreateDtoList.size());

            if (corpCreateDtoList.isEmpty()) {
                log.warn("API 호출 후 생성된 데이터가 없습니다. 저장 작업을 종료합니다.");
            }

            return corpCreateDtoList;
        } finally {
            stopWatch.stop();
        }
    }

    private int saveToDatabaseOptimized(List<CorpMastCreateDTO> corpCreateDtoList, String username, StopWatch stopWatch) {
        stopWatch.start("데이터베이스 저장");
        try {
            return saveCorpMastListOptimized(corpCreateDtoList, username);
        } finally {
            stopWatch.stop();
        }
    }

    private int saveCorpMastListOptimized(List<CorpMastCreateDTO> corpCreateDtoList, String username) {
        ProcessingResult processingResult = new ProcessingResult();
        List<String> duplicatedBizNos = new ArrayList<>();

        log.info("데이터베이스 저장 시작 - 총 {}건, 청크 크기: {}", corpCreateDtoList.size(), chunkSize);

        for (int i = 0; i < corpCreateDtoList.size(); i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, corpCreateDtoList.size());
            List<CorpMastCreateDTO> chunk = corpCreateDtoList.subList(i, endIndex);

            log.debug("청크 {} 처리 중: {} ~ {} (총 {}건)",
                    (i / chunkSize) + 1, i + 1, endIndex, chunk.size());

            try {
                processChunkWithNewTransaction(chunk, username, processingResult, duplicatedBizNos);
            } catch (Exception e) {
                log.error("청크 {} 처리 중 오류 발생", (i / chunkSize) + 1, e);
                processingResult.errorCount.addAndGet(chunk.size());
            }
        }

        logProcessingResults(processingResult, duplicatedBizNos);
        return processingResult.savedCount.get();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processChunkWithNewTransaction(List<CorpMastCreateDTO> chunk, String username,
                                                  ProcessingResult processingResult, List<String> duplicatedBizNos) {

        for (CorpMastCreateDTO dto : chunk) {
            try {
                processIndividualRecord(dto, username, processingResult, duplicatedBizNos);
            } catch (Exception ex) {
                log.error("개별 레코드 처리 중 예상치 못한 오류 발생: bizNo={}", dto.getBizNo(), ex);
                processingResult.errorCount.incrementAndGet();
                duplicatedBizNos.add(dto.getBizNo());
                saveHistoryRecord(username, ACTION_INSERT, dto.getBizNo(), RESULT_FAIL,
                        "예상치 못한 오류: " + ex.getMessage());
            }
        }
    }

    private void processIndividualRecord(CorpMastCreateDTO dto, String username,
                                         ProcessingResult processingResult, List<String> duplicatedBizNos) {
        try {
            CorpMast entity = dto.toEntity();
            String user = StringUtils.hasText(dto.getUsername()) ? dto.getUsername() : username;

            Optional<CorpMast> existingEntity = corpMastStore.findByBizNo(entity.getBizNo());

            if (existingEntity.isPresent()) {
                handleDuplicateRecord(entity.getBizNo(), user, processingResult, duplicatedBizNos);
            } else {
                saveNewRecord(entity, user, processingResult);
            }

        } catch (DataIntegrityViolationException ex) {
            handleDataIntegrityViolation(dto, username, processingResult, duplicatedBizNos, ex);
        } catch (Exception ex) {
            handleUnexpectedException(dto, username, processingResult, duplicatedBizNos, ex);
        }
    }

    private void handleDuplicateRecord(String bizNo, String username,
                                       ProcessingResult processingResult, List<String> duplicatedBizNos) {
        log.debug("이미 존재하는 bizNo로 인해 저장 스킵: {}", bizNo);
        duplicatedBizNos.add(bizNo);
        processingResult.duplicateCount.incrementAndGet();
        saveHistoryRecord(username, ACTION_INSERT, bizNo, RESULT_DUPLICATE, "중복 bizNo, 저장 스킵");
    }

    private void saveNewRecord(CorpMast entity, String username, ProcessingResult processingResult) {
        corpMastStore.save(entity);
        processingResult.savedCount.incrementAndGet();
        saveHistoryRecord(username, ACTION_INSERT, entity.getBizNo(), RESULT_SUCCESS, "정상 저장");
    }

    private void handleDataIntegrityViolation(CorpMastCreateDTO dto, String username,
                                              ProcessingResult processingResult, List<String> duplicatedBizNos,
                                              DataIntegrityViolationException ex) {
        log.warn("데이터베이스 저장 실패 (bizNo 중복): bizNo={}, 오류: {}", dto.getBizNo(), ex.getMessage());
        duplicatedBizNos.add(dto.getBizNo());
        processingResult.errorCount.incrementAndGet();
        saveHistoryRecord(username, ACTION_INSERT, dto.getBizNo(), RESULT_FAIL,
                "DataIntegrityViolationException: " + ex.getMessage());
    }

    private void handleUnexpectedException(CorpMastCreateDTO dto, String username,
                                           ProcessingResult processingResult, List<String> duplicatedBizNos,
                                           Exception ex) {
        log.error("데이터베이스 저장 중 예상치 못한 오류 발생: bizNo={}", dto.getBizNo(), ex);
        duplicatedBizNos.add(dto.getBizNo());
        processingResult.errorCount.incrementAndGet();
        saveHistoryRecord(username, ACTION_INSERT, dto.getBizNo(), RESULT_FAIL,
                "Exception: " + ex.getMessage());
    }

    private void logProcessingResults(ProcessingResult processingResult, List<String> duplicatedBizNos) {
        if (!duplicatedBizNos.isEmpty()) {
            log.info("데이터베이스에 저장되지 못한 건수 (중복 또는 오류): {}건", duplicatedBizNos.size());
            log.debug("저장 실패한 bizNo 목록: {}", duplicatedBizNos);
        }

        log.info("저장 완료 요약 - 성공: {}, 중복: {}, 오류: {}",
                processingResult.savedCount.get(),
                processingResult.duplicateCount.get(),
                processingResult.errorCount.get());
    }

    private void saveHistoryRecord(String username, String action, String bizNo, String result, String message) {
        try {
            CorpMastHistory history = CorpMastHistory.builder()
                    .username(username)
                    .action(action)
                    .bizNo(bizNo)
                    .result(result)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            corpMastHistoryStore.save(history);

        } catch (Exception e) {
            log.error("히스토리 저장 실패: action={}, bizNo={}", action, bizNo, e);
        }
    }

    private List<CorpMastCreateDTO> getCorpApiInfoOptimized(List<BizCsvInfoDto> csvList, String username) {
        log.info("API 호출 최적화 처리 시작 - 총 {}건, 최대 동시 처리: {}", csvList.size(), maxConcurrent);

        List<CompletableFuture<Optional<CorpMastCreateDTO>>> futures = csvList.stream()
                .map(csvInfo -> processAsyncWithRetry(csvInfo, username))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Async
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Optional<CorpMastCreateDTO>> processAsyncWithRetry(BizCsvInfoDto csvInfo, String username) {
        log.debug("processAsync 시작 (재시도 메커니즘 포함)");
        log.debug("입력 데이터 - bizNo: {}, bizNm: {}, bizAddress: {}",
                csvInfo.getBizNo(), csvInfo.getBizNm(), csvInfo.getBizAddress());

        try {
            CompletableFuture<String> corpFuture = corpApiClient.getCorpRegNo(csvInfo.getBizNo());
            CompletableFuture<RegionInfoDto> regionFuture = regionApiClient.getRegionInfo(csvInfo.getBizAddress());

            return corpFuture.thenCombine(regionFuture, (corpRegNo, regionInfo) -> {
                log.debug("법인등록번호 API 결과: bizNo={}, corpRegNo={}", csvInfo.getBizNo(), corpRegNo);
                log.debug("행정구역 API 결과: address={}, regionInfo={}", csvInfo.getBizAddress(), regionInfo);

                if (corpRegNo == null && regionInfo == null) {
                    log.debug("API 호출 실패: bizNo={}, bizNm={}. 빈 값으로 DTO 생성합니다.",
                            csvInfo.getBizNo(), csvInfo.getBizNm());
                    return Optional.of(createDefaultDto(csvInfo, username));
                }

                return Optional.of(createDtoFromApiResults(csvInfo, corpRegNo, regionInfo, username));

            }).exceptionally(ex -> {
                log.error("비동기 API 처리 중 예외 발생: bizNo={}, bizNm={}. 오류: {}",
                        csvInfo.getBizNo(), csvInfo.getBizNm(), ex.getMessage(), ex);
                return Optional.empty();
            });

        } catch (Exception e) {
            log.error("API 처리 중 예외 발생: bizNo={}", csvInfo.getBizNo(), e);
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    private CorpMastCreateDTO createDefaultDto(BizCsvInfoDto csvInfo, String username) {
        return CorpMastCreateDTO.builder()
                .sellerId(csvInfo.getSellerId())
                .bizNm(csvInfo.getBizNm())
                .bizNo(csvInfo.getBizNo())
                .corpRegNo("")
                .regionCd("")
                .siNm("")
                .sggNm("")
                .username(username)
                .build();
    }

    private CorpMastCreateDTO createDtoFromApiResults(BizCsvInfoDto csvInfo, String corpRegNo,
                                                      RegionInfoDto regionInfo, String username) {
        return CorpMastCreateDTO.builder()
                .sellerId(csvInfo.getSellerId())
                .bizNm(csvInfo.getBizNm())
                .bizNo(csvInfo.getBizNo())
                .corpRegNo(Optional.ofNullable(corpRegNo).orElse(""))
                .regionCd(regionInfo != null ? regionInfo.getRegionCd() : "")
                .siNm(regionInfo != null ? regionInfo.getSiNm() : "")
                .sggNm(regionInfo != null ? regionInfo.getSggNm() : "")
                .username(username)
                .build();
    }

    private static class ProcessingResult {
        private final AtomicInteger savedCount = new AtomicInteger(0);
        private final AtomicInteger duplicateCount = new AtomicInteger(0);
        private final AtomicInteger errorCount = new AtomicInteger(0);
    }
}