package com.antock.api.coseller.application.dto;

import com.antock.global.common.constants.CsvConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CsvService {

    @Value("${csv.file-template}")
    private String fileTemplate; // application.yml에서 설정한 파일 경로 템플릿
    public List<BizCsvInfo> readBizCsv(String city, String district) {
        String fileName = String.format(fileTemplate, city, district); //통신판매사업자_city_district.csv

        ClassPathResource resource = new ClassPathResource("csvFiles/" + fileName);

        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), Charset.forName("EUC-KR")));
        ) {
            return br.lines()
                    .skip(1)
                    .map(line -> line.split(",", -1)) //csv라인을 읽어서 String으로 넘김
                    .filter(this::isBiz)
                    .map(this::parseCsvData)
                    .toList();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * "법인여부" 가 "법인" 인지 필터링
     * @param tokens
     * @return
     */
    private boolean isBiz(String[] tokens) {
        return CsvConstants.CORP_TYPE_BIZ.equals(tokens[4].trim());
    }

    /**
     *
     * @param tokens
     * @return
     */
    private BizCsvInfo parseCsvData(String[] tokens) {

        return BizCsvInfo.builder()
                .sellerId(tokens[0].trim())
                .bizNm(tokens[2].trim())
                .bizNo(tokens[3].trim())
                .bizType(tokens[4].trim())
                .bizAddress(tokens[9].trim())
                .bizNesAddress(tokens[10].trim())
                .build();
    }

}
