package com.antock.api.csv.application.service;

import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CorpInfoApiClient;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import com.antock.api.csv.infrastructure.CsvFileWriter;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvBatchService {

    private final CorpInfoApiClient apiClient;
    private final CsvFileWriter fileWriter;
    private final CsvBatchHistoryRepository historyRepo;

    @Value("${batch.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${batch.retry.delay:1000}")
    private long retryDelay;

    @Value("${batch.chunk-size:1000}")
    private int chunkSize;

    @Value("${batch.max-concurrent:5}")
    private int maxConcurrent;

    private static final List<String> HEADERS = List.of(
            "sellerId", "bizNm", "bizNo", "bizType", "bizAddress", "bizNesAddress"
    );

    private static final Map<String, String> HEADER_TO_API_FIELD = Map.ofEntries(
            Map.entry("sellerId", "sellerId"),
            Map.entry("bizNm", "bizNm"),
            Map.entry("bizNo", "bizNo"),
            Map.entry("bizType", "bizType"),
            Map.entry("bizAddress", "bizAddress"),
            Map.entry("bizNesAddress", "bizNesAddress")
    );

    private static final Map<City, List<District>> CITY_DISTRICT_MAP = Map.ofEntries(
            Map.entry(City.서울특별시, List.of(District.강남구, District.강동구, District.강북구, District.서울강서구)),
            Map.entry(City.부산광역시, List.of(District.부산강서구, District.금정구, District.부산남구, District.부산동구)),
            Map.entry(City.대구광역시, List.of(District.대구남구, District.달서구, District.대구동구, District.대구북구)),
            Map.entry(City.인천광역시, List.of(District.계양구, District.인천남구, District.인천동구, District.미추홀구)),
            Map.entry(City.광주광역시, List.of(District.광산구, District.광주남구, District.광주동구, District.광주북구)),
            Map.entry(City.대전광역시, List.of(District.대덕구, District.대전동구, District.대전서구, District.유성구)),
            Map.entry(City.울산광역시, List.of(District.울산남구, District.울산동구, District.울산북구, District.울주군)),
            Map.entry(City.세종특별자치시, List.of(District.세종특별자치시)),
            Map.entry(City.경기도, List.of(District.가평군, District.고양시, District.과천시, District.광명시)),
            Map.entry(City.강원특별자치도, List.of(District.강릉시, District.강원고성군, District.동해시, District.삼척시)),
            Map.entry(City.충청북도, List.of(District.괴산군, District.단양군, District.보은군, District.영동군)),
            Map.entry(City.충청남도, List.of(District.계룡시, District.공주시, District.금산군, District.논산시)),
            Map.entry(City.전라북도, List.of(District.고창군, District.군산시, District.김제시, District.남원시)),
            Map.entry(City.전라남도, List.of(District.강진군, District.고흥군, District.곡성군, District.광양시)),
            Map.entry(City.경상북도, List.of(District.경산시, District.경주시, District.고령군, District.구미시)),
            Map.entry(City.경상남도, List.of(District.거제시, District.거창군, District.경남고성군, District.김해시)),
            Map.entry(City.제주특별자치도, List.of(District.제주시, District.서귀포시))
    );

    @Async
    public void runBatch() {
        log.info("CSV 배치 처리 시작");
        AtomicLong totalProcessed = new AtomicLong(0);
        AtomicLong totalSuccess = new AtomicLong(0);
        AtomicLong totalFailed = new AtomicLong(0);
        long startTime = System.currentTimeMillis();

        try {
            List<CompletableFuture<Void>> futures = CITY_DISTRICT_MAP.entrySet().stream()
                    .limit(maxConcurrent)
                    .map(entry -> CompletableFuture.runAsync(() ->
                            processCityDistricts(entry.getKey(), entry.getValue(), totalProcessed, totalSuccess, totalFailed)))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            log.info("CSV 배치 처리 완료 - 총 처리: {}, 성공: {}, 실패: {}, 소요시간: {}ms",
                    totalProcessed.get(), totalSuccess.get(), totalFailed.get(), totalTime);

        } catch (Exception e) {
            log.error("CSV 배치 처리 중 치명적 오류 발생", e);
            saveBatchHistory("BATCH_FAILED", "전체 배치 처리 실패: " + e.getMessage(), 0);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDistrict(City city, District district) {
        String cityName = city.name();
        String districtName = district.name();

        log.info("지역 처리 시작: {} {}", cityName, districtName);

        CsvBatchHistory history = CsvBatchHistory.builder()
                .city(cityName)
                .district(districtName)
                .status("PROCESSING")
                .timestamp(LocalDateTime.now())
                .build();

        try {

            List<Map<String, Object>> data = fetchDataWithRetry(cityName, districtName);

            if (data.isEmpty()) {
                log.warn("{} {} 지역에서 데이터를 찾을 수 없음", cityName, districtName);
                saveBatchHistory("NO_DATA", "데이터 없음", cityName, districtName, 0);
                return;
            }

            processDataInChunks(data, cityName, districtName);

            saveBatchHistory("SUCCESS", "처리 완료", cityName, districtName, data.size());

            log.info("{} {} 지역 처리 완료: {}건", cityName, districtName, data.size());

        } catch (Exception e) {
            log.error("{} {} 지역 처리 중 오류 발생", cityName, districtName, e);
            saveBatchHistory("ERROR", "처리 실패: " + e.getMessage(), cityName, districtName, 0);
            throw new BusinessException(ErrorCode.CORP_SEARCH_ERROR,
                    String.format("지역 %s %s 처리 실패: %s", cityName, districtName, e.getMessage()));
        }
    }

    private void processCityDistricts(City city, List<District> districts,
                                      AtomicLong totalProcessed, AtomicLong totalSuccess, AtomicLong totalFailed) {
        String cityName = city.name();
        log.info("도시 {} 처리 시작 ({}개 구/군)", cityName, districts.size());

        for (District district : districts) {
            try {
                processDistrict(city, district);
                totalSuccess.incrementAndGet();
            } catch (Exception e) {
                totalFailed.incrementAndGet();
                log.error("{} {} 처리 실패", cityName, district.name(), e);
            } finally {
                totalProcessed.incrementAndGet();
            }
        }
    }

    @Retryable(
            retryFor = {IOException.class, DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private List<Map<String, Object>> fetchDataWithRetry(String city, String district) {
        try {
            return apiClient.fetchAll(city, district);
        } catch (Exception e) {
            log.warn("API 호출 실패 (재시도 예정): {} {}, 오류: {}", city, district, e.getMessage());
            throw e;
        }
    }

    private void processDataInChunks(List<Map<String, Object>> data, String city, String district) {
        int totalSize = data.size();
        int chunkCount = (int) Math.ceil((double) totalSize / chunkSize);

        log.info("{} {} 데이터 청크 처리 시작: 총 {}건, 청크 크기: {}, 청크 수: {}",
                city, district, totalSize, chunkSize, chunkCount);

        for (int i = 0; i < chunkCount; i++) {
            int startIndex = i * chunkSize;
            int endIndex = Math.min(startIndex + chunkSize, totalSize);

            List<Map<String, Object>> chunk = data.subList(startIndex, endIndex);

            try {
                processChunk(chunk, city, district, i + 1, chunkCount);
            } catch (Exception e) {
                log.error("청크 {} 처리 실패: {} {}", i + 1, city, district, e);
                throw new BusinessException(ErrorCode.CORP_SEARCH_ERROR,
                        String.format("청크 %d 처리 실패: %s", i + 1, e.getMessage()));
            }
        }
    }

    private void processChunk(List<Map<String, Object>> chunk, String city, String district,
                              int chunkNumber, int totalChunks) {
        try {
            String fileName = String.format("%s_%s_chunk_%d_of_%d.csv", city, district, chunkNumber, totalChunks);

            fileWriter.writeCsv(city, district, HEADERS, chunk, HEADER_TO_API_FIELD);

            log.debug("청크 {} 처리 완료: {}건", chunkNumber, chunk.size());

        } catch (IOException e) {
            log.error("청크 {} 파일 작성 실패", chunkNumber, e);
            throw new BusinessException(ErrorCode.CORP_SEARCH_ERROR,
                    String.format("청크 %d 파일 작성 실패: %s", chunkNumber, e.getMessage()));
        }
    }

    private void saveBatchHistory(String status, String message, String city, String district, int recordCount) {
        try {
            CsvBatchHistory history = CsvBatchHistory.builder()
                    .city(city)
                    .district(district)
                    .fileName(String.format("%s_%s_batch.csv", city, district))
                    .recordCount(recordCount)
                    .status(status)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            historyRepo.save(history);

        } catch (Exception e) {
            log.error("배치 이력 저장 실패", e);
        }
    }

    private void saveBatchHistory(String status, String message, int recordCount) {
        try {
            CsvBatchHistory history = CsvBatchHistory.builder()
                    .city("ALL")
                    .district("ALL")
                    .fileName("batch_summary.csv")
                    .recordCount(recordCount)
                    .status(status)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            historyRepo.save(history);

        } catch (Exception e) {
            log.error("배치 이력 저장 실패", e);
        }
    }
}