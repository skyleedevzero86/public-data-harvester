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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoSellerService {

    private final CsvService csvService;
    private final CorpApiClient corpApiClient;
    private final RegionApiClient regionApiClient;
    private final CorpMastStore corpMastStore;
    private final CorpMastHistoryStore corpMastHistoryStore;
    private final Executor asyncExecutor;

    public CsvService getCsvService() {
        return csvService;
    }

    @Transactional
    public int saveCoSeller(RegionRequestDto requestDto, String username) {
        String fileName = requestDto.getCity().getValue() + "_" + requestDto.getDistrict().getValue() + ".csv";
        List<BizCsvInfoDto> csvList = csvService.readCsvFile(fileName);
        if (csvList.isEmpty()) {
            return 0;
        }
        int batchSize = 100;
        int totalSaved = 0;
        for (int i = 0; i < csvList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, csvList.size());
            List<BizCsvInfoDto> batch = csvList.subList(i, end);
            List<CorpMastCreateDTO> corpCreateDtoList = getCorpApiInfo(batch, username);
            if (!corpCreateDtoList.isEmpty()) {
                int savedCnt = saveCorpMastList(corpCreateDtoList, username);
                totalSaved += savedCnt;
            }
        }
        return totalSaved;
    }

    @Transactional
    public int saveCoSeller(String city, String district, String username) {
        String fileName = city + "_" + district + ".csv";
        List<BizCsvInfoDto> csvList;
        try {
            csvList = csvService.readCsvFile(fileName);
            if (csvList.isEmpty()) {
                return 0;
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV 파일을 읽을 수 없습니다: " + fileName, e);
        }

        int batchSize = 100;
        int totalSaved = 0;
        for (int i = 0; i < csvList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, csvList.size());
            List<BizCsvInfoDto> batch = csvList.subList(i, end);
            List<CorpMastCreateDTO> corpCreateDtoList = getCorpApiInfo(batch, username);
            if (!corpCreateDtoList.isEmpty()) {
                int savedCnt = saveCorpMastList(corpCreateDtoList, username);
                totalSaved += savedCnt;
            }
        }
        return totalSaved;
    }

    private int saveCorpMastList(List<CorpMastCreateDTO> corpCreateDtoList, String username) {
        int savedCount = 0;
        List<String> duplicatedBizNos = new ArrayList<>();
        List<String> bizNos = corpCreateDtoList.stream()
                .map(CorpMastCreateDTO::getBizNo)
                .collect(Collectors.toList());
        List<String> existingBizNos = corpMastStore.findExistingBizNos(bizNos);
        for (CorpMastCreateDTO dto : corpCreateDtoList) {
            CorpMast entity = dto.toEntity();
            String user = dto.getUsername() != null ? dto.getUsername() : username;
            try {
                if (existingBizNos.contains(entity.getBizNo())) {
                    duplicatedBizNos.add(entity.getBizNo());
                    CompletableFuture.runAsync(() -> {
                        try {
                            CorpMastHistory history = CorpMastHistory.builder()
                                    .username(user)
                                    .action("INSERT")
                                    .bizNo(entity.getBizNo())
                                    .result("DUPLICATE")
                                    .message("중복 bizNo, 저장 스킵")
                                    .timestamp(java.time.LocalDateTime.now())
                                    .build();
                            corpMastHistoryStore.save(history);
                        } catch (Exception e) {
                        }
                    }, asyncExecutor);
                } else {
                    corpMastStore.save(entity);
                    savedCount++;
                    CompletableFuture.runAsync(() -> {
                        try {
                            CorpMastHistory history = CorpMastHistory.builder()
                                    .username(user)
                                    .action("INSERT")
                                    .bizNo(entity.getBizNo())
                                    .result("SUCCESS")
                                    .message("정상 저장")
                                    .timestamp(java.time.LocalDateTime.now())
                                    .build();
                            corpMastHistoryStore.save(history);
                        } catch (Exception e) {
                        }
                    }, asyncExecutor);
                }
            } catch (DataIntegrityViolationException ex) {
                duplicatedBizNos.add(entity.getBizNo());
                CompletableFuture.runAsync(() -> {
                    try {
                        CorpMastHistory history = CorpMastHistory.builder()
                                .username(user)
                                .action("INSERT")
                                .bizNo(entity.getBizNo())
                                .result("FAIL")
                                .message("DataIntegrityViolationException: " + ex.getMessage())
                                .timestamp(java.time.LocalDateTime.now())
                                .build();
                        corpMastHistoryStore.save(history);
                    } catch (Exception e) {
                    }
                }, asyncExecutor);
            } catch (Exception ex) {
                duplicatedBizNos.add(entity.getBizNo());
                CompletableFuture.runAsync(() -> {
                    try {
                        CorpMastHistory history = CorpMastHistory.builder()
                                .username(user)
                                .action("INSERT")
                                .bizNo(entity.getBizNo())
                                .result("FAIL")
                                .message("Exception: " + ex.getMessage())
                                .timestamp(java.time.LocalDateTime.now())
                                .build();
                        corpMastHistoryStore.save(history);
                    } catch (Exception e) {
                    }
                }, asyncExecutor);
            }
        }
        return savedCount;
    }

    private List<CorpMastCreateDTO> getCorpApiInfo(List<BizCsvInfoDto> csvList, String username) {
        List<CorpMastCreateDTO> corpCreateDtoList = new ArrayList<>();
        for (BizCsvInfoDto csvInfo : csvList) {
            try {
                CorpMastCreateDTO dto = generateMockCorpData(csvInfo, username);
                corpCreateDtoList.add(dto);
            } catch (Exception e) {
            }
        }
        return corpCreateDtoList;
    }

    private CorpMastCreateDTO generateMockCorpData(BizCsvInfoDto csvInfo, String username) {
        String corpRegNo = generateMockCorpRegNo();
        String regionCd = generateMockRegionCd();
        String[] addressParts = csvInfo.getBizAddress().split(" ");
        String siNm = addressParts.length > 0 ? addressParts[0] : "서울특별시";
        String sggNm = addressParts.length > 1 ? addressParts[1] : "강남구";

        return CorpMastCreateDTO.builder()
                .sellerId(csvInfo.getSellerId())
                .bizNm(csvInfo.getBizNm())
                .bizNo(csvInfo.getBizNo())
                .corpRegNo(corpRegNo)
                .regionCd(regionCd)
                .siNm(siNm)
                .sggNm(sggNm)
                .username(username)
                .repNm(generateMockRepName())
                .estbDt(generateMockEstbDate())
                .roadNmAddr(csvInfo.getBizAddress())
                .jibunAddr(csvInfo.getBizNesAddress())
                .corpStatus("정상영업")
                .build();
    }

    private String generateMockCorpRegNo() {
        Random random = new Random();
        int year = 2020 + random.nextInt(5);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        int sequence = 1000000 + random.nextInt(9000000);

        return String.format("%04d%02d%02d-%07d", year, month, day, sequence);
    }

    private String generateMockRegionCd() {
        Random random = new Random();
        return String.format("%05d", 10000 + random.nextInt(90000));
    }

    private String generateMockRepName() {
        String[] surnames = { "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임" };
        String[] givenNames = { "철수", "영희", "민수", "수진", "동현", "지영", "현우", "서연", "준호", "미영" };

        Random random = new Random();
        String surname = surnames[random.nextInt(surnames.length)];
        String givenName = givenNames[random.nextInt(givenNames.length)];

        return surname + givenName;
    }

    private String generateMockEstbDate() {
        Random random = new Random();
        int year = 1990 + random.nextInt(35);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);

        return String.format("%04d%02d%02d", year, month, day);
    }

    @Transactional
    public int clearAllData() {
        List<CorpMast> allCorps = corpMastStore.findAll();
        int totalCount = allCorps.size();

        for (CorpMast corp : allCorps) {
            corpMastStore.delete(corp);
        }
        return totalCount;
    }

    @Async
    public CompletableFuture<Optional<CorpMastCreateDTO>> processAsync(BizCsvInfoDto csvInfo, String username) {
        try {
            CompletableFuture<String> corpRegNoFuture = corpApiClient.getCorpRegNo(csvInfo.getBizNo());
            CompletableFuture<RegionInfoDto> regionInfoFuture = regionApiClient.getRegionInfo(csvInfo.getBizAddress());
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(corpRegNoFuture, regionInfoFuture);
            return allFutures.thenApply(v -> {
                String corpRegNo = corpRegNoFuture.join();
                RegionInfoDto regionInfo = regionInfoFuture.join();
                if (isValidCorpRegNo(corpRegNo) && isValidRegionInfo(regionInfo)) {
                    return Optional.of(CorpMastCreateDTO.builder()
                            .sellerId(csvInfo.getSellerId())
                            .bizNm(csvInfo.getBizNm())
                            .bizNo(csvInfo.getBizNo())
                            .corpRegNo(corpRegNo)
                            .regionCd(regionInfo.getRegionCd())
                            .siNm(regionInfo.getSiNm())
                            .sggNm(regionInfo.getSggNm())
                            .username(username)
                            .repNm(csvInfo.getOwnerName())
                            .estbDt(csvInfo.getDate())
                            .roadNmAddr(csvInfo.getBizAddress())
                            .jibunAddr(csvInfo.getBizNesAddress())
                            .corpStatus("계속(수익)")
                            .build());
                }
                return Optional.<CorpMastCreateDTO>empty();
            });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    private boolean isValidCorpRegNo(String corpRegNo) {
        return corpRegNo != null &&
                !corpRegNo.trim().isEmpty() &&
                !corpRegNo.startsWith("0") &&
                !corpRegNo.contains("N/A");
    }

    private boolean isValidRegionInfo(RegionInfoDto regionInfo) {
        return regionInfo != null &&
                regionInfo.getRegionCd() != null &&
                !regionInfo.getRegionCd().trim().isEmpty() &&
                !regionInfo.getRegionCd().startsWith("0");
    }

    @Transactional
    public void processBatch(List<BizCsvInfoDto> csvList, String username) {
        int batchSize = 100;
        for (int i = 0; i < csvList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, csvList.size());
            List<BizCsvInfoDto> batch = csvList.subList(i, end);
            List<CorpMastCreateDTO> dtoList = getCorpApiInfo(batch, username);
            if (!dtoList.isEmpty()) {
                saveCorpMastList(dtoList, username);
            }
        }
    }
}