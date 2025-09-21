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
        List<CompletableFuture<Optional<CorpMastCreateDTO>>> futures = csvList.stream()
                .map(csvInfo -> processAsync(csvInfo, username))
                .collect(Collectors.toList());
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
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