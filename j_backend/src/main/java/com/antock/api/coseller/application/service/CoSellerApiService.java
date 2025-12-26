package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.client.CorpApiClient;
import com.antock.api.coseller.application.client.RegionApiClient;
import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoSellerApiService {

    private final CorpApiClient corpApiClient;
    private final RegionApiClient regionApiClient;
    private final CoSellerDataMapper dataMapper;

    @Async
    public CompletableFuture<Optional<CorpMastCreateDTO>> processAsync(BizCsvInfoDto csvInfo, String username) {
        try {
            CompletableFuture<String> corpRegNoFuture = corpApiClient.getCorpRegNo(csvInfo.getBizNo());
            CompletableFuture<RegionInfoDto> regionInfoFuture = regionApiClient.getRegionInfo(csvInfo.getBizAddress());
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(corpRegNoFuture, regionInfoFuture);

            return allFutures.thenApply(v -> {
                String corpRegNo = corpRegNoFuture.join();
                RegionInfoDto regionInfo = regionInfoFuture.join();
                return dataMapper.mapFromApiData(csvInfo, corpRegNo, regionInfo, username);
            });
        } catch (Exception e) {
            log.warn("비동기 처리 실패: bizNo={}", csvInfo.getBizNo(), e);
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }
}

