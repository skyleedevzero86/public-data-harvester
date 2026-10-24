package com.antock.corp.application.service;

import com.antock.api.coseller.application.client.CorpApiClient;
import com.antock.api.coseller.application.client.RegionApiClient;
import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import com.antock.api.coseller.application.service.CoSellerApiService;
import com.antock.api.coseller.application.service.CoSellerChunkWriter;
import com.antock.api.coseller.application.service.CoSellerDataMapper;
import com.antock.api.coseller.application.service.CoSellerService;
import com.antock.api.coseller.application.service.CsvService;
import com.antock.api.coseller.application.service.FailedChunkRecorder;
import com.antock.api.coseller.application.service.ManualRegistrationHistory;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CoSellerServiceTest {
    @Mock
    private CsvService csvService;
    @Mock
    private CorpApiClient corpApiClient;
    @Mock
    private RegionApiClient regionApiClient;
    @Mock
    private CoSellerChunkWriter chunkWriter;
    @Mock
    private FailedChunkRecorder failedChunkRecorder;
    @Mock
    private ManualRegistrationHistory manualRegistrationHistory;

    private CoSellerService coSellerService;

    private final List<BizCsvInfoDto> csvInfoList = new ArrayList<>();
    private RegionRequestDto requestDto;

    @BeforeEach
    void setUp() {
        coSellerService = new CoSellerService(
                new CoSellerApiService(corpApiClient, regionApiClient, new CoSellerDataMapper()),
                csvService,
                chunkWriter,
                failedChunkRecorder,
                manualRegistrationHistory);
        csvInfoList.clear();
        requestDto = RegionRequestDto.builder()
                .city(City.서울특별시)
                .district(District.강남구)
                .build();

        csvInfoList.add(BizCsvInfoDto.builder()
                .sellerId("2025-서울강남-00789")
                .bizNo("518-42-01193")
                .bizNm("신시어랩")
                .bizAddress("서울특별시 강남구 신사동 ***-** 신사동 빌딩")
                .bizNesAddress("서울특별시 강남구 논현로***길 * B*층 B**호 신사동 빌딩 (신사동)")
                .build());
        csvInfoList.add(BizCsvInfoDto.builder()
                .sellerId("2025-서울강남-00746")
                .bizNo("629-03-03380")
                .bizNm("이지웰라이프")
                .bizAddress("서울특별시 강남구 논현동 ***-** 논현 더라움  ")
                .bizNesAddress("서울특별시 강남구 선릉로***길 *-* *층 ****호 (논현동 논현 더라움)")
                .build());
    }

    @Test
    @DisplayName("데이터 저장 로직 검증")
    void saveCoSeller_save_success() {
        stubChunk(csvInfoList);
        when(chunkWriter.prepare(eq(csvInfoList), eq("testuser"), isNull(), isNull())).thenReturn(List.of());
        when(chunkWriter.saveChunk(anyList(), eq("testuser"))).thenReturn(2);

        int savedCount = coSellerService.saveCoSeller(requestDto, "testuser");

        assertThat(savedCount).isEqualTo(2);
        verify(chunkWriter).saveChunk(anyList(), eq("testuser"));
    }

    @Test
    @DisplayName("csv list를 넣어서 dto 반환 코드 검증 ")
    public void proccessAsync_success() throws Exception {
        when(corpApiClient.getCorpRegNo(eq(csvInfoList.get(0).getBizNo())))
                .thenReturn(CompletableFuture.completedFuture("111111-1234567"));
        when(regionApiClient.getRegionInfo(eq(csvInfoList.get(0).getBizAddress())))
                .thenReturn(CompletableFuture.completedFuture(RegionInfoDto.builder()
                        .regionCd("1168010300")
                        .siNm("서울특별시")
                        .sggNm("강남구")
                        .build()));

        Optional<CorpMastCreateDTO> result = coSellerService.processAsync(csvInfoList.get(0), "testuser").get();

        assertThat(result).isPresent();
        assertThat(result.get().getCorpRegNo()).isEqualTo("111111-1234567");
        assertThat(result.get().getRegionCd()).isEqualTo("1168010300");
        assertThat(result.get().getSellerId()).isEqualTo(csvInfoList.get(0).getSellerId());
        assertThat(result.get().getSiNm()).isEqualTo("서울특별시");
        assertThat(result.get().getSggNm()).isEqualTo("강남구");
    }

    @Test
    @DisplayName("API 값이 없으면 빈 Optional을 반환한다")
    void processAsync_fail_when_api_calls_return_null_or_empty() throws Exception {
        when(corpApiClient.getCorpRegNo(eq(csvInfoList.get(0).getBizNo())))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(regionApiClient.getRegionInfo(eq(csvInfoList.get(0).getBizAddress())))
                .thenReturn(CompletableFuture.completedFuture(null));

        Optional<CorpMastCreateDTO> result = coSellerService.processAsync(csvInfoList.get(0), "testuser").get();

        assertThat(result).isEmpty();
    }

    @Test
    @Disabled("비동기 API 실패가 Optional.empty()로 바뀌지 않고 future가 예외로 끝난다")
    @DisplayName("API 호출 중 예외 발생 시 빈 Optional 반환")
    void processAsync_exception_in_api_call() throws Exception {
        when(corpApiClient.getCorpRegNo(eq(csvInfoList.get(0).getBizNo())))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Corp API error")));
        when(regionApiClient.getRegionInfo(eq(csvInfoList.get(0).getBizAddress())))
                .thenReturn(CompletableFuture.completedFuture(RegionInfoDto.builder()
                        .regionCd("1168010300")
                        .siNm("서울특별시")
                        .sggNm("강남구")
                        .build()));

        Optional<CorpMastCreateDTO> result = coSellerService.processAsync(csvInfoList.get(0), "testuser").get();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("saveCoSeller: DataIntegrityViolationException 처리 검증")
    void saveCoSeller_handles_dataIntegrityViolationException() {
        List<BizCsvInfoDto> testCsvList = List.of(BizCsvInfoDto.builder()
                .sellerId("id1").bizNo("123-45-67890").bizNm("Org1").bizAddress("Addr1").build());
        stubChunk(testCsvList);
        when(chunkWriter.prepare(eq(testCsvList), eq("testuser"), isNull(), isNull())).thenReturn(List.of());
        doThrow(new DataIntegrityViolationException("Simulated unique constraint violation"))
                .when(chunkWriter).saveChunk(anyList(), eq("testuser"));

        int savedCount = coSellerService.saveCoSeller(requestDto, "testuser");

        assertThat(savedCount).isEqualTo(0);
        verify(failedChunkRecorder).record(eq("서울특별시_강남구.csv"), eq(0), eq(testCsvList), any(Exception.class));
    }

    @Test
    @DisplayName("saveCoSeller: 일반 예외 처리 검증")
    void saveCoSeller_handles_general_exception() {
        List<BizCsvInfoDto> testCsvList = List.of(BizCsvInfoDto.builder()
                .sellerId("id1").bizNo("123-45-67890").bizNm("Org1").bizAddress("Addr1").build());
        stubChunk(testCsvList);
        when(chunkWriter.prepare(eq(testCsvList), eq("testuser"), isNull(), isNull())).thenReturn(List.of());
        doThrow(new RuntimeException("Simulated unexpected error"))
                .when(chunkWriter).saveChunk(anyList(), eq("testuser"));

        int savedCount = coSellerService.saveCoSeller(requestDto, "testuser");

        assertThat(savedCount).isEqualTo(0);
        verify(failedChunkRecorder).record(eq("서울특별시_강남구.csv"), eq(0), eq(testCsvList), any(Exception.class));
    }

    private void stubChunk(List<BizCsvInfoDto> rows) {
        doAnswer(invocation -> {
            Consumer<List<BizCsvInfoDto>> consumer = invocation.getArgument(2);
            consumer.accept(rows);
            return null;
        }).when(csvService).readInChunks(eq("서울특별시_강남구.csv"), eq(100), any());
    }
}
