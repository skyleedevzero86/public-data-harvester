package com.antock.global.common.exception;

import org.springframework.http.HttpStatus;

public class CsvParsingException extends CustomException {

    public CsvParsingException(HttpStatus httpStatus, String resultMsg) {
        super(httpStatus, resultMsg);
    }

    public CsvParsingException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public CsvParsingException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}