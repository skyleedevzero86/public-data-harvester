package com.antock.global.common.exception;

import org.springframework.http.HttpStatus;

public class CsvParsingException extends CustomException{

    public CsvParsingException(HttpStatus httpStatus, String resultMsg) {
        super(httpStatus, resultMsg);
    }
}