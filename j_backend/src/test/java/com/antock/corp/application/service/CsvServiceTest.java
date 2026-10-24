package com.antock.corp.application.service;

import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.service.CsvService;
import com.antock.api.coseller.application.service.strategy.CsvFileReadStrategy;
import com.antock.global.common.constants.CsvConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(csvService, "csvEncoding", "UTF-8");
        ReflectionTestUtils.setField(csvService, "bufferSize", 8192);
        ReflectionTestUtils.setField(csvService, "maxLines", 10000);
    }

    @Test
    @DisplayName("csv 파일 조회 검증")
    public void read_csv_file_test() throws Exception {
        String csvData = csvRow("서울특별시 강남구");
        when(csvFileReadStrategy.readFile(anyString()))
                .thenReturn(new ByteArrayInputStream(csvData.getBytes(Charset.forName("EUC-KR"))));

        long startTime = System.currentTimeMillis();
        List<BizCsvInfoDto> list = csvService.readCsvFile("test.csv");
        long endTime = System.currentTimeMillis();
        System.out.println("소요시간 = " + (endTime - startTime) + " ms");

        assertNotNull(list, "결과 리스트는 null이 아니어야 합니다");
        assertThat(list).isNotEmpty().hasSize(1);

        BizCsvInfoDto info = list.get(0);
        assertThat(info.getBizType()).isEqualTo(CsvConstants.CORP_TYPE_BIZ);
        assertThat(info.getBizNm()).isEqualTo("주식회사 뮤직터빈");
    }

    @Test
    @DisplayName("csv 없는 경우 DEFAULT CSV로 동일 시나리오 진행")
    void readCsvFile_no_File() throws Exception {
        String defaultCsvData = csvRow("대구광역시 강남구");
        when(csvFileReadStrategy.readFile(anyString()))
                .thenReturn(new ByteArrayInputStream(defaultCsvData.getBytes(Charset.forName("EUC-KR"))));

        List<BizCsvInfoDto> list = csvService.readCsvFile("default.csv");

        assertNotNull(list, "결과 리스트는 null이 아니어야 합니다");
        assertThat(list).isNotEmpty().hasSize(1);

        BizCsvInfoDto info = list.get(0);
        assertThat(info.getBizType()).isEqualTo(CsvConstants.CORP_TYPE_BIZ);
        assertThat(info.getBizNm()).isEqualTo("주식회사 뮤직터빈");
    }

    private static String csvRow(String address) {
        return "sellerId,city,bizNm,bizNo,bizType,owner,phone,email,date,address\n"
                + "1,서울특별시,주식회사 뮤직터빈,1234567890123," + CsvConstants.CORP_TYPE_BIZ
                + ",홍길동,010,a@b.c,20200101," + address;
    }
}
