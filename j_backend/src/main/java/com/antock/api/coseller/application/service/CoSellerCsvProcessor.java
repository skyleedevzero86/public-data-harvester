package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoSellerCsvProcessor {

    private final CsvService csvService;

    public List<BizCsvInfoDto> readCsvByRegion(RegionRequestDto requestDto) {
        String fileName = requestDto.getCity().getValue() + "_" + requestDto.getDistrict().getValue() + ".csv";
        return csvService.readCsvFile(fileName);
    }

    public List<BizCsvInfoDto> readCsvByCityAndDistrict(String city, String district) {
        String fileName = city + "_" + district + ".csv";
        try {
            return csvService.readCsvFile(fileName);
        } catch (Exception e) {
            throw new RuntimeException("CSV 파일을 읽을 수 없습니다: " + fileName, e);
        }
    }
}

