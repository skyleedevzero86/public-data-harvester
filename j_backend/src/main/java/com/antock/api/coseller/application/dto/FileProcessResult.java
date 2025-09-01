package com.antock.api.coseller.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileProcessResult {
    private int processedCount;
    private int savedCount;
    private String error;
}