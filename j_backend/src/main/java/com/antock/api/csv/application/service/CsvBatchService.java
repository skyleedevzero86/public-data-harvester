package com.antock.api.csv.application.service;

import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import com.antock.api.csv.infrastructure.CsvFileWriter;
import com.antock.api.csv.infrastructure.CorpInfoApiClient;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CsvBatchService {
    private final CorpInfoApiClient apiClient;
    private final CsvFileWriter fileWriter;
    private final CsvBatchHistoryRepository historyRepo;

    private static final List<String> HEADERS = List.of(
            "통신판매번호", "신고기관명", "상호", "사업자등록번호", "법인여부", "대표자명", "전화번호", "전자우편", "신고일자", "사업장소재지", "사업장소재지(도로명)", "업소상태", "신고기관", "대표연락처", "판매방식", "취급품목", "인터넷도메인", "호스트서버소재지"
    );

    @Transactional
    public void runBatch() {
        for (City city : City.values()) {
            for (District district : District.values()) {
                String cityName = city.getValue();
                String districtName = district.getValue();
                String fileName = fileWriter.getFilePath(cityName, districtName);

                if (fileWriter.fileExists(cityName, districtName)) {
                    historyRepo.save(CsvBatchHistory.builder()
                            .city(cityName)
                            .district(districtName)
                            .fileName(fileName)
                            .recordCount(0)
                            .status("SKIPPED")
                            .message("이미 파일 존재")
                            .timestamp(LocalDateTime.now())
                            .build());
                    continue;
                }
                try {
                    List<Map<String, Object>> data = apiClient.fetchAll(cityName, districtName);
                    if (data.isEmpty()) {
                        historyRepo.save(CsvBatchHistory.builder()
                                .city(cityName)
                                .district(districtName)
                                .fileName(fileName)
                                .recordCount(0)
                                .status("FAIL")
                                .message("API 데이터 없음 또는 에러")
                                .timestamp(LocalDateTime.now())
                                .build());
                        continue;
                    }
                    fileWriter.writeCsv(cityName, districtName, HEADERS, data);
                    historyRepo.save(CsvBatchHistory.builder()
                            .city(cityName)
                            .district(districtName)
                            .fileName(fileName)
                            .recordCount(data.size())
                            .status("SUCCESS")
                            .message("정상 저장")
                            .timestamp(LocalDateTime.now())
                            .build());
                } catch (Exception e) {
                    historyRepo.save(CsvBatchHistory.builder()
                            .city(cityName)
                            .district(districtName)
                            .fileName(fileName)
                            .recordCount(0)
                            .status("FAIL")
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
                }
            }
        }
    }
}