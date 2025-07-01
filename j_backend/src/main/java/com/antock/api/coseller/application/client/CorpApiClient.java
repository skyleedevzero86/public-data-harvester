package com.antock.api.coseller.application.client;

import java.util.concurrent.CompletableFuture;

/**
 * 추후 API 변경을 고려하여 법인등록번호 조회 INTERFACE생성
 */
public interface CorpApiClient {
    CompletableFuture<String> getCorpRegNo(String bizNo);
}
