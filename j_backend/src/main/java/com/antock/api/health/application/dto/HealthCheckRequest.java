package com.antock.api.health.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "헬스 체크 요청 DTO")
public class HealthCheckRequest {

    @Schema(description = "체크할 컴포넌트 목록", example = "[\"database\", \"redis\", \"cache\"]")
    private List<String> components;

    @Schema(description = "체크 타입", example = "manual", allowableValues = { "manual", "scheduled", "api" })
    private String checkType;

    @Schema(description = "상세 정보 포함 여부", example = "true")
    private boolean includeDetails;

    @Schema(description = "캐시 무시 여부", example = "false")
    private boolean ignoreCache;
}
