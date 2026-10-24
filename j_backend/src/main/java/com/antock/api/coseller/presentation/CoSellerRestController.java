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
        try {
            String username = (user != null) ? user.getUsername() : "system";
            int savedCount = cosellerService.saveCoSeller(regionRequestDto, username);
            return ApiResponse.success(savedCount, "데이터 저장이 완료되었습니다.");
        } catch (Exception e) {
            log.error("코셀러 데이터 저장 실패", e);
            return ApiResponse.error("데이터 저장에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/save-simple")
    public ApiResponse<Integer> saveCoSellerSimple(
            @Valid @RequestBody RegionRequestDto regionRequestDto,
            @CurrentUser AuthenticatedUser user) {
        try {
            String city = regionRequestDto.getCity().getValue();
            String district = regionRequestDto.getDistrict().getValue();

            String username = (user != null) ? user.getUsername() : "system";
            int savedCount = cosellerService.saveCoSeller(city, district, username);

            if (savedCount > 0) {
                return ApiResponse.success(savedCount, String.format("데이터 수집이 완료되었습니다. %d건이 저장되었습니다.", savedCount));
            } else {
                return ApiResponse.success(0, "해당 지역의 CSV 파일을 찾을 수 없거나 데이터가 없습니다.");
            }
        } catch (Exception e) {
            log.error("코셀러 데이터 저장 실패: {} {} - {}",
                    regionRequestDto.getCity(), regionRequestDto.getDistrict(), e.getMessage(), e);
            return ApiResponse.error("데이터 저장에 실패했습니다: " + e.getMessage());
        }
    }
}