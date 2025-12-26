package com.antock.api.coseller.application.client;

import java.util.concurrent.CompletableFuture;

public interface CorpApiClient {
    CompletableFuture<String> getCorpRegNo(String bizNo);
}
