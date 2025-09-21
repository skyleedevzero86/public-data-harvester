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

import java.util.Map;

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
            @RequestBody Map<String, String> request,
            @CurrentUser AuthenticatedUser user) {
        try {
            String city = request.get("city");
            String district = request.get("district");

            if (city == null || district == null) {
                return ApiResponse.error("시/도와 구/군 정보가 필요합니다.");
            }

            String username = (user != null) ? user.getUsername() : "system";
            int savedCount = cosellerService.saveCoSeller(city, district, username);

            if (savedCount > 0) {
                return ApiResponse.success(savedCount, String.format("데이터 수집이 완료되었습니다. %d건이 저장되었습니다.", savedCount));
            } else {
                return ApiResponse.success(0, "해당 지역의 CSV 파일을 찾을 수 없거나 데이터가 없습니다.");
            }
        } catch (Exception e) {
            log.error("코셀러 데이터 저장 실패: {} {} - {}",
                    request.get("city"), request.get("district"), e.getMessage(), e);
            return ApiResponse.error("데이터 저장에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/debug/minio-files")
    public ApiResponse<Map<String, Object>> debugMinioFiles() {
        try {
            var csvService = cosellerService.getCsvService();
            var fileReadStrategy = csvService.getFileReadStrategy();

            if (fileReadStrategy instanceof com.antock.api.coseller.application.service.strategy.MinioFileReadStrategy) {
                var minioStrategy = (com.antock.api.coseller.application.service.strategy.MinioFileReadStrategy) fileReadStrategy;
                boolean isAvailable = minioStrategy.isMinioAvailable();

                Map<String, Object> result = Map.of(
                        "minioAvailable", isAvailable,
                        "message", isAvailable ? "MinIO 연결 성공" : "MinIO 연결 실패",
                        "timestamp", System.currentTimeMillis());

                return ApiResponse.success(result, "MinIO 상태 확인 완료");
            } else {
                Map<String, Object> result = Map.of(
                        "message", "MinIO 전략이 아닙니다: " + fileReadStrategy.getClass().getSimpleName(),
                        "timestamp", System.currentTimeMillis());

                return ApiResponse.success(result, "현재 파일 읽기 전략 확인");
            }
        } catch (Exception e) {
            log.error("MinIO 디버깅 실패", e);
            return ApiResponse.error("MinIO 디버깅에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/debug/test-file-read")
    public ApiResponse<Map<String, Object>> testFileRead(
            @RequestParam String city,
            @RequestParam String district) {
        String fileName = city + "_" + district + ".csv";
        try {
            var csvService = cosellerService.getCsvService();
            var csvList = csvService.readCsvFile(fileName);

            Map<String, Object> result = Map.of(
                    "fileName", fileName,
                    "recordCount", csvList.size(),
                    "success", true,
                    "message", "파일 읽기 성공");
            return ApiResponse.success(result, "파일 읽기 테스트 성공");
        } catch (Exception e) {
            Map<String, Object> result = Map.of(
                    "fileName", fileName,
                    "error", e.getMessage(),
                    "success", false,
                    "message", "파일 읽기 실패");
            return ApiResponse.success(result, "파일 읽기 테스트 실패");
        }
    }

    @GetMapping("/debug/minio-file-list")
    public ApiResponse<Map<String, Object>> getMinioFileList() {
        try {
            var csvService = cosellerService.getCsvService();
            var fileReadStrategy = csvService.getFileReadStrategy();

            if (fileReadStrategy instanceof com.antock.api.coseller.application.service.strategy.MinioFileReadStrategy) {
                var minioStrategy = (com.antock.api.coseller.application.service.strategy.MinioFileReadStrategy) fileReadStrategy;
                var minioClient = minioStrategy.getMinioClient();
                var bucketName = minioStrategy.getBucketName();

                var objects = minioClient.listObjects(
                        io.minio.ListObjectsArgs.builder()
                                .bucket(bucketName)
                                .build());

                var fileList = new java.util.ArrayList<String>();
                for (var result : objects) {
                    var item = result.get();
                    fileList.add(item.objectName());
                }

                Map<String, Object> result = Map.of(
                        "bucketName", bucketName,
                        "fileCount", fileList.size(),
                        "files", fileList,
                        "message", "MinIO 파일 목록 조회 성공");

                return ApiResponse.success(result, "MinIO 파일 목록을 성공적으로 조회했습니다.");
            } else {
                Map<String, Object> result = Map.of(
                        "message", "MinIO 전략이 아닙니다: " + fileReadStrategy.getClass().getSimpleName());
                return ApiResponse.success(result, "현재 파일 읽기 전략 확인");
            }
        } catch (Exception e) {
            log.error("MinIO 파일 목록 조회 실패", e);
            return ApiResponse.error("MinIO 파일 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/debug/test-insert")
    public ApiResponse<Map<String, Object>> testInsert(
            @RequestParam String city,
            @RequestParam String district) {
        try {
            String username = "test-user";
            int savedCount = cosellerService.saveCoSeller(city, district, username);

            Map<String, Object> result = Map.of(
                    "success", true,
                    "savedCount", savedCount,
                    "city", city,
                    "district", district,
                    "username", username);

            return ApiResponse.success(result, "데이터 삽입 테스트가 완료되었습니다.");
        } catch (Exception e) {
            log.error("데이터 삽입 테스트 실패", e);
            Map<String, Object> errorResult = Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "city", city,
                    "district", district);
            return ApiResponse.error("데이터 삽입 테스트에 실패했습니다: " + e.getMessage(), errorResult);
        }
    }

    @DeleteMapping("/debug/clear-data")
    public ApiResponse<Map<String, Object>> clearData() {
        try {
            int deletedCount = cosellerService.clearAllData();

            Map<String, Object> result = Map.of(
                    "success", true,
                    "deletedCount", deletedCount);

            return ApiResponse.success(result, "데이터 정리가 완료되었습니다.");
        } catch (Exception e) {
            log.error("데이터 정리 실패", e);
            Map<String, Object> errorResult = Map.of(
                    "success", false,
                    "error", e.getMessage());
            return ApiResponse.error("데이터 정리에 실패했습니다: " + e.getMessage(), errorResult);
        }
    }
}