package com.antock.service;

import com.antock.api.coseller.application.CsvService;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import com.antock.global.common.constants.CsvConstants;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class CsvServiceTest {

    @Autowired
    private CsvService csvService;

    private City city;
    private District district;
    @BeforeEach
    void setUp() {
        city = City.valueOf("서울특별시");
        district = District.valueOf("강남구");

    }

    @Test
    @DisplayName("csv 파일 조회 검증")
    public void read_csv_file_test() throws Exception {
        //given

        //when
        long startTime =  System.currentTimeMillis();
        List<BizCsvInfoDto> list = csvService.readBizCsv(city.name(), district.name());


        long endTime =  System.currentTimeMillis();
        System.out.println("소요시간 = " + (endTime - startTime) + " ms");
        //then
        assertNotNull(list);

        BizCsvInfoDto info = list.get(0);
        Assertions.assertThat(CsvConstants.CORP_TYPE_BIZ).isEqualTo(info.getBizType());
    }
}
