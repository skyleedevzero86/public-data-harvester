package com.antock.api.coseller.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DataCollectionResult {
    private boolean success;
    private String message;
    private int totalProcessed;
    private int totalSaved;
    private List<String> errors;
}