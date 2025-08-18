package com.antock.coseller.application.service;

import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.service.CoSellerService;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CoSellerServicePerformanceTest {

    @Autowired
    private CoSellerService coSellerService;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(10);
    }

    @Test
    void testBulkProcessingPerformance() throws Exception {
        int csvItemCount = 1000;
        List<BizCsvInfoDto> csvList = generateTestCsvData(csvItemCount);

        long startTime = System.currentTimeMillis();

        coSellerService.processBatch(csvList, "testUser");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("배치 처리 성능: " + csvItemCount + "개 항목을 " + duration + "ms에 처리");
        System.out.println("평균 처리 시간: " + (duration / (double) csvItemCount) + "ms per item");
    }

    @Test
    void testConcurrentProcessing() throws Exception {
        int concurrentRequests = 10;
        int itemsPerRequest = 100;
        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrentRequests; i++) {
            RegionRequestDto request = RegionRequestDto.builder()
                    .city(City.서울특별시)
                    .district(District.강남구)
                    .build();

            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return coSellerService.saveCoSeller(request, "testUser" + i);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executorService);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(60, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        int totalProcessed = futures.stream()
                .mapToInt(CompletableFuture::join)
                .sum();

        System.out.println("동시 처리 성능: " + concurrentRequests + "개 요청을 " + duration + "ms에 처리");
        System.out.println("총 처리된 항목: " + totalProcessed);
        System.out.println("평균 처리 시간: " + (duration / (double) concurrentRequests) + "ms per request");
    }

    private List<BizCsvInfoDto> generateTestCsvData(int count) {
        List<BizCsvInfoDto> csvList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BizCsvInfoDto csvInfo = BizCsvInfoDto.builder()
                    .sellerId("seller" + i)
                    .bizNm("테스트기업" + i)
                    .bizNo("123456789" + String.format("%03d", i))
                    .bizType("개인")
                    .bizAddress("서울특별시 강남구 테헤란로 123")
                    .bizNesAddress("서울특별시 강남구 테헤란로 123")
                    .build();
            csvList.add(csvInfo);
        }
        return csvList;
    }
}