package com.antock.api.coseller.application.client;

import com.antock.api.coseller.application.dto.api.RegionInfoDto;

import java.util.concurrent.CompletableFuture;

public interface RegionApiClient {
    CompletableFuture<String> getRegionCode(String address);
    CompletableFuture<RegionInfoDto> getRegionInfo(String address);
}