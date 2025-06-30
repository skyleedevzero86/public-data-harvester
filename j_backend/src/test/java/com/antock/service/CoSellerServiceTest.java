package com.antock.service;

import com.antock.api.coseller.application.CoSellerService;
import com.antock.api.coseller.application.CorpApiService;
import com.antock.api.coseller.application.CsvService;
import com.antock.api.coseller.application.RegionApiService;
import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CoSellerServiceTest {
    @Mock
    private CsvService csvService;
    @Mock private CorpApiService corpApiService;
    @Mock private RegionApiService regionApiService;

    @InjectMocks
    private CoSellerService coSellerService;

    private final List<BizCsvInfoDto> csvInfoList = new ArrayList<>();

    public CoSellerServiceTest() {
    }

    @BeforeEach
    void setUp() {
        BizCsvInfoDto csvInfo1 = BizCsvInfoDto.builder()
                .sellerId("2025-서울강남-00789")
                .bizNo("518-42-01193")
                .bizNm("신시어랩")
                .bizAddress("서울특별시 강남구 신사동 ***-** 신사동 빌딩")
                .bizNesAddress("서울특별시 강남구 논현로***길 * B*층 B**호 신사동 빌딩 (신사동)")
                .build();

        BizCsvInfoDto csvInfo2 = BizCsvInfoDto.builder()
                .sellerId("2025-서울강남-00746")
                .bizNo("629-03-03380")
                .bizNm("이지웰라이프")
                .bizAddress("서울특별시 강남구 논현동 ***-** 논현 더라움  ")
                .bizNesAddress("서울특별시 강남구 선릉로***길 *-* *층 ****호 (논현동 논현 더라움)")
                .build();

        csvInfoList.add(csvInfo1);
        csvInfoList.add(csvInfo2);

    }

    @Test
    @DisplayName("csv list를 넣어서 dto 반환 코드 검증 ")
    public void proccessAsync_success() throws Exception {
        //given
        //법인 코드 조회
        when(corpApiService.getCorpRegNo(csvInfoList.get(0).getBizNo())).thenReturn(
                CompletableFuture.completedFuture("111111-1234567")
        );
        // 행정 구역 코드 조회
        when(regionApiService.getRegionCode(csvInfoList.get(0).getBizAddress())).thenReturn(
                CompletableFuture.completedFuture("1168010300")
        );
        //when
        Optional<CorpMastCreateDTO> result = coSellerService.processAsync(csvInfoList.get(0)).get();
        //then
        assertThat(result).isPresent();
        assertThat(result.get().getCorpRegNo()).isEqualTo("111111-1234567");
        assertThat(result.get().getRegionCd()).isEqualTo("1168010300");
        assertThat(result.get().getSellerId()).isEqualTo(csvInfoList.get(0).getSellerId());

    }

    @Test
    @DisplayName("API둘다 실패시 csvInfo 데이터를 가진 CorpMastCreateDTO 반환")
    void processAsync_fail_when_corpApi_fails() throws Exception {
        // given
        when(corpApiService.getCorpRegNo(csvInfoList.get(0).getBizNo()))
                .thenReturn(CompletableFuture.completedFuture(null)); // 실패 시 null

        when(regionApiService.getRegionCode(csvInfoList.get(0).getBizAddress()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // when
        CompletableFuture<Optional<CorpMastCreateDTO>> future = coSellerService.processAsync(csvInfoList.get(0));
        Optional<CorpMastCreateDTO> result = future.get();

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCorpRegNo()).isEqualTo("");
        assertThat(result.get().getRegionCd()).isEqualTo("");
        assertThat(result.get().getSellerId()).isEqualTo(csvInfoList.get(0).getSellerId());

    }

}
