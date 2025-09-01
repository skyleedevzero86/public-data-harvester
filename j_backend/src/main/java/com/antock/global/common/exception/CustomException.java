package com.antock.global.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String resultMsg;

    public CustomException(HttpStatus httpStatus, String resultMsg) {
        super(resultMsg);
        this.httpStatus = httpStatus;
        this.resultMsg = resultMsg;
    }

    public CustomException(HttpStatus httpStatus, String resultMsg, Throwable cause) {
        super(resultMsg, cause);
        this.httpStatus = httpStatus;
        this.resultMsg = resultMsg;
    }
}