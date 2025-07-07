package com.antock.api.coseller.presentation;

import com.antock.api.coseller.application.service.CoSellerService;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.global.common.response.ApiResponse;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        String username = user != null ? user.getUsername() : "anonymous";
        log.info("요청한 내용: {}, username: {}", regionRequestDto, user != null ? user.getUsername() : "anonymous");
        log.info("사용자: {}", user);
        return ApiResponse.of(HttpStatus.OK, cosellerService.saveCoSeller(regionRequestDto, username));
    }
}