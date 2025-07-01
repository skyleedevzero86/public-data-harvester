package com.antock.api.coseller.presentation;

import com.antock.api.coseller.application.service.CoSellerService;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public ApiResponse<Integer> saveCoSeller(@Valid @RequestBody RegionRequestDto regionRequestDto) {
        log.info("Received request: {}", regionRequestDto);
        return ApiResponse.of(HttpStatus.OK, cosellerService.saveCoSeller(regionRequestDto));
    }
}