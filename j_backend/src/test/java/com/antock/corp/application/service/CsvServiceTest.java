package com.antock.corp.application.service;

import com.antock.api.coseller.application.service.CsvService;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import com.antock.global.common.constants.CsvConstants;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CSV파일 조회 및 관리 Service Test
 */
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "csv.file-template=%s_%s.csv")
@Import(CsvService.class)
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
        assertThat(CsvConstants.CORP_TYPE_BIZ).isEqualTo(info.getBizType());
    }

    @Test
    @DisplayName("csv 없는경우 DEFAULT CSV로 동일 시나리오 진행")
    void readBizCsv_no_File() {
        // 없는 경로의 파일 요청
        List<BizCsvInfoDto> list = csvService.readBizCsv("대구광역시", "강남구");

        BizCsvInfoDto info = list.get(0);
        assertThat(CsvConstants.CORP_TYPE_BIZ).isEqualTo(info.getBizType());
        assertThat(info.getBizNm()).isEqualTo("주식회사 뮤직터빈");
    }
}
