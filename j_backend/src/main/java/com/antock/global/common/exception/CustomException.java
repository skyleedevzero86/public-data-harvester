package com.antock.global.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends  RuntimeException{
    private HttpStatus httpStatus;
    private String resultMsg;

    public CustomException(HttpStatus httpStatus, String resultMsg) {
        this.httpStatus = httpStatus;
        this.resultMsg = resultMsg;
    }
    public CustomException(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }
}