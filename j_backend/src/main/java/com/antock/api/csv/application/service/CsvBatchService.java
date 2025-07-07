package com.antock.api.csv.application.service;

import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import com.antock.api.csv.infrastructure.CsvFileWriter;
import com.antock.api.csv.infrastructure.CorpInfoApiClient;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CsvBatchService {
    private final CorpInfoApiClient apiClient;
    private final CsvFileWriter fileWriter;
    private final CsvBatchHistoryRepository historyRepo;

    private static final List<String> HEADERS = List.of(
            "통신판매번호", "신고기관명", "상호", "사업자등록번호", "법인여부", "대표자명", "전화번호", "전자우편", "신고일자", "사업장소재지", "사업장소재지(도로명)", "업소상태", "신고기관", "대표연락처", "판매방식", "취급품목", "인터넷도메인", "호스트서버소재지"
    );

    private static final Map<String, String> HEADER_TO_API_FIELD = Map.ofEntries(
            Map.entry("통신판매번호", "prmmiMnno"),
            Map.entry("신고기관명", "dclrInstNm"),
            Map.entry("상호", "bzmnNm"),
            Map.entry("사업자등록번호", "brno"),
            Map.entry("법인여부", "corpYnNm"),
            Map.entry("대표자명", "rprsvNm"),
            Map.entry("전화번호", "telno"),
            Map.entry("전자우편", "rprsvEmladr"),
            Map.entry("신고일자", "dclrDate"),
            Map.entry("사업장소재지", "lctnAddr"),
            Map.entry("사업장소재지(도로명)", "lctnRnAddr"),
            Map.entry("업소상태", "operSttusCdNm"),
            Map.entry("신고기관", "prcsDeptNm"),
            Map.entry("대표연락처", "chrgDeptTelno"),
            Map.entry("판매방식", "ntslMthdNm"),
            Map.entry("취급품목", "trtmntPrdlstNm"),
            Map.entry("인터넷도메인", "domnCn"),
            Map.entry("호스트서버소재지", "opnServerPlaceAladr")
    );

    private static final Map<City, List<District>> CITY_DISTRICT_MAP = Map.of(
            City.서울특별시, List.of(
                    District.강남구, District.강동구, District.강북구, District.강서구, District.관악구, District.광진구, District.구로구, District.금천구,
                    District.노원구, District.도봉구, District.동대문구, District.동작구, District.마포구, District.서대문구, District.서초구,
                    District.성동구, District.성북구, District.송파구, District.양천구, District.영등포구, District.용산구, District.은평구,
                    District.종로구, District.중구, District.중랑구
            ),
            City.제주특별자치도, List.of(District.제주시, District.서귀포시)
            // ... (각 시/도별 실제 구/군 추가)
    );

    public void runBatch() {
        for (Map.Entry<City, List<District>> entry : CITY_DISTRICT_MAP.entrySet()) {
            City city = entry.getKey();
            for (District district : entry.getValue()) {
                processDistrict(city, district);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDistrict(City city, District district) {
        String cityName = city.getValue();
        String districtName = district.getValue();
        String fileName = fileWriter.getFilePath(cityName, districtName);

        try {
            if (fileWriter.fileExists(cityName, districtName)) {
                historyRepo.saveAndFlush(CsvBatchHistory.builder()
                        .city(cityName)
                        .district(districtName)
                        .fileName(fileName)
                        .recordCount(0)
                        .status("SKIPPED")
                        .message("이미 파일 존재")
                        .timestamp(LocalDateTime.now())
                        .build());
                return;
            }

            List<Map<String, Object>> apiData = apiClient.fetchAll(cityName, districtName);

            List<Map<String, Object>> filtered = apiData.stream()
                    .filter(row -> {
                        String dclrInstNm = Objects.toString(row.get("dclrInstNm"), "");
                        String lctnAddr = Objects.toString(row.get("lctnAddr"), "");
                        String lctnRnAddr = Objects.toString(row.get("lctnRnAddr"), "");
                        String prmmiMnno = Objects.toString(row.get("prmmiMnno"), "");
                        String prmmiYr = Objects.toString(row.get("prmmiYr"), "");

                        String[] parts = prmmiMnno.split("-");
                        String extractedGu = "";
                        if (parts.length >= 3) {
                            extractedGu = parts[1]; 
                        }

                        String districtShort = districtName.replace("구", "").replace("군", "").replace("시", "");

                        if (!extractedGu.isEmpty()) {
                            return extractedGu.contains(districtShort);
                        }

                        return dclrInstNm.contains(districtName) ||
                                lctnAddr.contains(districtName) ||
                                lctnRnAddr.contains(districtName);
                    })
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                historyRepo.saveAndFlush(CsvBatchHistory.builder()
                        .city(cityName)
                        .district(districtName)
                        .fileName(fileName)
                        .recordCount(0)
                        .status("FAIL")
                        .message("API 데이터 없음 또는 에러")
                        .timestamp(LocalDateTime.now())
                        .build());
                return;
            }

            List<Map<String, Object>> processed = filtered.stream()
                    .map(row -> {
                        Map<String, Object> newRow = new HashMap<>(row);
                        String prmmiYr = Objects.toString(row.get("prmmiYr"), "");
                        String prmmiMnno = Objects.toString(row.get("prmmiMnno"), "");

                        if (!prmmiMnno.isEmpty()) {
                            char firstChar = prmmiMnno.charAt(0);
                            if (Character.UnicodeBlock.of(firstChar) == Character.UnicodeBlock.HANGUL_SYLLABLES ||
                                    Character.UnicodeBlock.of(firstChar) == Character.UnicodeBlock.HANGUL_JAMO ||
                                    Character.UnicodeBlock.of(firstChar) == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
                                newRow.put("prmmiMnno", (!prmmiYr.isEmpty() ? prmmiYr + "-" : "") + prmmiMnno);
                            } else if (Character.isDigit(firstChar)) {
                                newRow.put("prmmiMnno", prmmiMnno);
                            } else {
                                newRow.put("prmmiMnno", (!prmmiYr.isEmpty() ? prmmiYr + "-" : "") + prmmiMnno);
                            }
                        } else {
                            newRow.put("prmmiMnno", "");
                        }
                        return newRow;
                    })
                    .collect(Collectors.toList());

            fileWriter.writeCsv(cityName, districtName, HEADERS, processed, HEADER_TO_API_FIELD);

            historyRepo.saveAndFlush(CsvBatchHistory.builder()
                    .city(cityName)
                    .district(districtName)
                    .fileName(fileName)
                    .recordCount(processed.size())
                    .status("SUCCESS")
                    .message("정상 저장")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            historyRepo.saveAndFlush(CsvBatchHistory.builder()
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