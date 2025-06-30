package com.antock.api.coseller.application;

import com.antock.api.coseller.application.dto.BizCsvInfo;
import com.antock.api.coseller.application.dto.CsvService;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class CoSellerService {

    private final CsvService csvService;

    public String saveCoSeller(RegionRequestDto requestDto){

        //City와 disctrict로 csv 파일 읽어오기
        List<BizCsvInfo> list = csvService.readBizCsv(requestDto.getCity().name(), requestDto.getDistrict().name());

        return "";
    }
}
