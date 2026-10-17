package com.antock.api.csv.infrastructure;

public class CorpApiResponseException extends RuntimeException {

    public CorpApiResponseException(String message) {
        super(message);
    }

    public CorpApiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
