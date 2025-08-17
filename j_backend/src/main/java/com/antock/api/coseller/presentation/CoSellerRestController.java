package com.antock.api.coseller.presentation;

import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.service.CoSellerService;
import com.antock.global.common.response.ApiResponse;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/coseller")
public class CoSellerRestController {

    private final CoSellerService cosellerService;

    @PostMapping("/save")
    public ApiResponse<Integer> saveCoSeller(
            @Valid @RequestBody RegionRequestDto regionRequestDto,
            @CurrentUser AuthenticatedUser user) {

        log.info("코셀러 데이터 저장 요청: {}", regionRequestDto);

        try {
            int savedCount = cosellerService.saveCoSeller(regionRequestDto, user.getUsername());
            return ApiResponse.success(savedCount, "데이터 저장이 완료되었습니다.");
        } catch (Exception e) {
            log.error("코셀러 데이터 저장 실패", e);
            return ApiResponse.error("데이터 저장에 실패했습니다: " + e.getMessage());
        }
    }
}