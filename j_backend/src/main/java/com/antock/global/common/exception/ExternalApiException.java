package com.antock.global.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExternalApiException extends CustomException {
    private final String externalApiName;
    private final String endpoint;

    public ExternalApiException(HttpStatus httpStatus, String resultMsg, String externalApiName, String endpoint) {
        super(httpStatus, resultMsg);
        this.externalApiName = externalApiName;
        this.endpoint = endpoint;
    }

    // @Override
    // public HttpStatus getStatus() {
    // return super.getStatus();
    // }
}