package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.client.RegionApiClient;
import com.antock.api.coseller.application.dto.api.RegionApiJsonResponse;
import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import com.antock.api.coseller.application.dto.api.RegionJuso;
import com.antock.api.coseller.application.dto.properties.RegionApiProperties;
import com.antock.global.common.exception.ExternalApiException;
import com.antock.global.utils.AddressUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionApiService implements RegionApiClient {

    private final RestTemplate restTemplate;
    private final RegionApiProperties regionProp;
    private final ObjectMapper objectMapper;

    @Async
    public CompletableFuture<String> getRegionCode(String address) {
        String fomattedAddress = AddressUtil.extractAddress(address);
        URI requestUrl = regionProp.buildRequestUrlWithAddress(fomattedAddress);
        log.debug("주소 '{}'에 대한 행정구역 코드 API 요청: {}", fomattedAddress, requestUrl);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            log.debug("주소 '{}'에 대한 행정구역 API 응답: {}", address, response.getBody());
            log.info("행정구역 API 응답 성공");

            if (response.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.completedFuture(parseRegionCode(response.getBody()));
            } else {
                log.error("행정구역 API 응답 실패: status={}, body={}", response.getStatusCode(), response.getBody());
                return CompletableFuture.completedFuture("");
            }
        } catch (ResourceAccessException e) {
            log.error("네트워크 오류 또는 타임아웃 발생 (행정구역 API): {}", e.getMessage(), e);
            return CompletableFuture.completedFuture("");
        } catch (RestClientException e) {
            log.error("REST 클라이언트 오류 발생 (행정구역 API): {}", e.getMessage(), e);
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "행정구역 코드 API 통신에 실패하였습니다.", e);

        } catch (Exception e) {
            log.error("행정구역 API 호출 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "행정구역 코드 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    @Async
    @Override
    public CompletableFuture<RegionInfoDto> getRegionInfo(String address) {
        String fomattedAddress = AddressUtil.extractAddress(address);
        URI requestUrl = regionProp.buildRequestUrlWithAddress(fomattedAddress);
        log.debug("주소 '{}'에 대한 행정구역 정보 API 요청: {}", fomattedAddress, requestUrl);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            log.debug("주소 '{}'에 대한 행정구역 정보 API 응답: {}", address, response.getBody());
            log.info("행정구역 정보 API 응답 성공");

            if (response.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.completedFuture(parseRegionInfo(response.getBody()));
            } else {
                log.error("행정구역 정보 API 응답 실패: status={}, body={}", response.getStatusCode(), response.getBody());
                return CompletableFuture.completedFuture(new RegionInfoDto());
            }
        } catch (ResourceAccessException e) {
            log.error("네트워크 오류 또는 타임아웃 발생 (행정구역 정보 API): {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(new RegionInfoDto());
        } catch (RestClientException e) {
            log.error("REST 클라이언트 오류 발생 (행정구역 정보 API): {}", e.getMessage(), e);
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "행정구역 정보 API 통신에 실패하였습니다.", e);
        } catch (Exception e) {
            log.error("행정구역 정보 API 호출 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "행정구역 정보 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    private String parseRegionCode(String json) {
        try {
            RegionApiJsonResponse response = objectMapper.readValue(json, RegionApiJsonResponse.class);

            if (response == null || response.getResults() == null) {
                log.warn("행정구역 API 응답에 results 필드가 없거나 null입니다. JSON: {}", json);
                return "";
            }

            List<RegionJuso> jusoList = response.getResults().getJuso();

            if (jusoList == null || jusoList.isEmpty()) {
                log.warn("행정구역 API 응답에 juso 리스트가 없거나 비어있습니다. JSON: {}", json);
                return "";
            }

            RegionJuso juso = jusoList.get(0);
            String regionCd = juso.getAdmCd();
            String siNm = juso.getSiNm();
            String sggNm = juso.getSggNm();

            log.debug("파싱된 행정구역 코드: {}", regionCd);
            log.debug("파싱된 시/도명: {}", siNm);
            log.debug("파싱된 시/군/구명: {}", sggNm);

            return regionCd;

        } catch (Exception e) {
            log.error("행정구역 API 응답 파싱 실패: 오류 = {}, JSON = {}", e.getMessage(), json, e);
        }

        return "";
    }

    private RegionInfoDto parseRegionInfo(String json) {
        try {
            RegionApiJsonResponse response = objectMapper.readValue(json, RegionApiJsonResponse.class);

            if (response == null || response.getResults() == null) {
                log.warn("행정구역 정보 API 응답에 results 필드가 없거나 null입니다. JSON: {}", json);
                return new RegionInfoDto();
            }

            List<RegionJuso> jusoList = response.getResults().getJuso();

            if (jusoList == null || jusoList.isEmpty()) {
                log.warn("행정구역 정보 API 응답에 juso 리스트가 없거나 비어있습니다. JSON: {}", json);
                return new RegionInfoDto();
            }

            RegionJuso juso = jusoList.get(0);
            String regionCd = juso.getAdmCd();
            String siNm = juso.getSiNm();
            String sggNm = juso.getSggNm();
            String corpRegNo = juso.getCorpRegNo();

            log.debug("파싱된 행정구역 코드: {}", regionCd);
            log.debug("파싱된 시/도명: {}", siNm);
            log.debug("파싱된 시/군/구명: {}", sggNm);
            log.debug("파싱된 법정동 코드: {}", corpRegNo);

            // RegionInfoDto 생성 및 반환
            return RegionInfoDto.builder()
                    .regionCd(regionCd)
                    .siNm(siNm)
                    .sggNm(sggNm)
                    .corpRegNo(corpRegNo)
                    .build();

        } catch (Exception e) {
            log.error("행정구역 정보 API 응답 파싱 실패: 오류 = {}, JSON = {}", e.getMessage(), json, e);
        }

        return new RegionInfoDto();
    }
}