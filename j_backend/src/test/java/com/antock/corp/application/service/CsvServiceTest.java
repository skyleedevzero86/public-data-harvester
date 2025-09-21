package com.antock.corp.application.service;

import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.service.CsvService;
import com.antock.api.coseller.application.service.strategy.CsvFileReadStrategy;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import com.antock.global.common.constants.CsvConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CsvServiceTest {

    @Mock
    private CsvFileReadStrategy csvFileReadStrategy;

    @InjectMocks
    private CsvService csvService;

    private City city;
    private District district;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        city = City.valueOf("서울특별시");
        district = District.valueOf("강남구");

        // fileTemplate 필드를 리플렉션으로 설정
        Field fileTemplateField = CsvService.class.getDeclaredField("fileTemplate");
        fileTemplateField.setAccessible(true);
        fileTemplateField.set(csvService, "%s_%s.csv");
    }

    @Test
    @DisplayName("csv 파일 조회 검증")
    public void read_csv_file_test() throws Exception {
        // Given
        String csvData = "id,,bizNm,bizNo,bizType,,address,nesAddress\n" +
                "1,,주식회사 뮤직터빈,1234567890123,법인,,서울특별시 강남구,강남구 테헤란로";
        when(csvFileReadStrategy.getBufferedReader(anyString()))
                .thenReturn(new BufferedReader(new StringReader(csvData)));

        // When
        long startTime = System.currentTimeMillis();
        List<BizCsvInfoDto> list = csvService.readCsvFile("test.csv");
        long endTime = System.currentTimeMillis();
        System.out.println("소요시간 = " + (endTime - startTime) + " ms");

        // Then
        assertNotNull(list, "결과 리스트는 null이 아니어야 합니다");
        assertThat(list).isNotEmpty().hasSize(1);

        BizCsvInfoDto info = list.get(0);
        assertThat(info.getBizType()).isEqualTo(CsvConstants.CORP_TYPE_BIZ);
        assertThat(info.getBizNm()).isEqualTo("주식회사 뮤직터빈");
    }

    @Test
    @DisplayName("csv 없는 경우 DEFAULT CSV로 동일 시나리오 진행")
    void readCsvFile_no_File() throws Exception {
        // Given
        String defaultCsvData = "id,,bizNm,bizNo,bizType,,address,nesAddress\n" +
                "1,,주식회사 뮤직터빈,1234567890123,법인,,대구광역시 강남구,강남구 테헤란로";
        when(csvFileReadStrategy.getBufferedReader(anyString()))
                .thenReturn(new BufferedReader(new StringReader(defaultCsvData)));

        // When
        List<BizCsvInfoDto> list = csvService.readCsvFile("default.csv");

        // Then
        assertNotNull(list, "결과 리스트는 null이 아니어야 합니다");
        assertThat(list).isNotEmpty().hasSize(1);

        BizCsvInfoDto info = list.get(0);
        assertThat(info.getBizType()).isEqualTo(CsvConstants.CORP_TYPE_BIZ);
        assertThat(info.getBizNm()).isEqualTo("주식회사 뮤직터빈");
    }
}