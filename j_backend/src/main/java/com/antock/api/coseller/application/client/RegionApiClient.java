package com.antock.api.coseller.application.client;

import java.util.concurrent.CompletableFuture;

/**
 * 추후 API 변경을 고려하여 행정구역코드 조회 INTERFACE생성
 */
public interface RegionApiClient {
    CompletableFuture<String> getRegionCode(String address);
}