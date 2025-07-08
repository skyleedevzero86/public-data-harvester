package com.antock.corp.application.service;

import com.antock.api.coseller.application.service.CoSellerService;
import com.antock.api.coseller.application.service.CsvService;
import com.antock.api.coseller.application.client.CorpApiClient;
import com.antock.api.coseller.application.client.RegionApiClient;
import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import com.antock.api.coseller.infrastructure.CorpMastStore;
import com.antock.api.coseller.infrastructure.CorpMastHistoryStore;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.mockito.quality.Strictness;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class CoSellerServiceTest {
        @Mock
        private CsvService csvService;
        @Mock
        private CorpApiClient corpApiClient;
        @Mock
        private RegionApiClient regionApiClient;
        @Mock
        private CorpMastStore corpMastStore;
        @Mock
        private CorpMastHistoryStore corpMastHistoryStore;

        @InjectMocks
        private CoSellerService coSellerService;

        private final List<BizCsvInfoDto> csvInfoList = new ArrayList<>();
        private RegionRequestDto requestDto;

        @BeforeEach
        void setUp() {
                requestDto = RegionRequestDto.builder()
                        .city(City.서울특별시)
                        .district(District.강남구)
                        .build();

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

                doNothing().when(corpMastHistoryStore).save(any());
        }

        @Test
        @DisplayName("데이터 저장 로직 검증")
        void saveCoSeller_save_success() throws Exception {
                // given
                when(csvService.readBizCsv("서울특별시", "강남구"))
                        .thenReturn(csvInfoList);

                when(corpApiClient.getCorpRegNo(anyString()))
                        .thenReturn(CompletableFuture.completedFuture("111111-1234567"));

                when(regionApiClient.getRegionInfo(anyString()))
                        .thenReturn(CompletableFuture.completedFuture(RegionInfoDto.builder()
                                .regionCd("1168010300")
                                .siNm("서울특별시")
                                .sggNm("강남구")
                                .build()));

                when(corpMastStore.findByBizNo(csvInfoList.get(0).getBizNo())).thenReturn(Optional.empty());
                when(corpMastStore.findByBizNo(csvInfoList.get(1).getBizNo())).thenReturn(Optional.empty());

                doNothing().when(corpMastStore).save(any(CorpMast.class));

                // when
                int savedCount = coSellerService.saveCoSeller(requestDto, "testuser");

                // then
                assertThat(savedCount).isEqualTo(2);
                verify(corpMastStore, times(2)).save(any(CorpMast.class));
                verify(corpMastHistoryStore, times(2)).save(any());
        }

        @Test
        @DisplayName("csv list를 넣어서 dto 반환 코드 검증 ")
        public void proccessAsync_success() throws Exception {
                // given
                when(corpApiClient.getCorpRegNo(eq(csvInfoList.get(0).getBizNo())))
                        .thenReturn(CompletableFuture.completedFuture("111111-1234567"));

                when(regionApiClient.getRegionInfo(eq(csvInfoList.get(0).getBizAddress())))
                        .thenReturn(CompletableFuture.completedFuture(RegionInfoDto.builder()
                                .regionCd("1168010300")
                                .siNm("서울특별시")
                                .sggNm("강남구")
                                .build()));

                // when
                Optional<CorpMastCreateDTO> result = coSellerService.processAsync(csvInfoList.get(0), "testuser").get();

                // then
                assertThat(result).isPresent();
                assertThat(result.get().getCorpRegNo()).isEqualTo("111111-1234567");
                assertThat(result.get().getRegionCd()).isEqualTo("1168010300");
                assertThat(result.get().getSellerId()).isEqualTo(csvInfoList.get(0).getSellerId());
                assertThat(result.get().getSiNm()).isEqualTo("서울특별시");
                assertThat(result.get().getSggNm()).isEqualTo("강남구");
        }

        @Test
        @DisplayName("API둘다 실패시 csvInfo 데이터를 가진 CorpMastCreateDTO 반환")
        void processAsync_fail_when_api_calls_return_null_or_empty() throws Exception {
                // given
                when(corpApiClient.getCorpRegNo(anyString()))
                        .thenReturn(CompletableFuture.completedFuture(null));

                when(regionApiClient.getRegionInfo(anyString()))
                        .thenReturn(CompletableFuture.completedFuture(null));

                // when
                CompletableFuture<Optional<CorpMastCreateDTO>> future = coSellerService
                        .processAsync(csvInfoList.get(0), "testuser");
                Optional<CorpMastCreateDTO> result = future.get();

                // then
                assertThat(result).isPresent();
                assertThat(result.get().getCorpRegNo()).isEqualTo("");
                assertThat(result.get().getRegionCd()).isEqualTo("");
                assertThat(result.get().getSiNm()).isEqualTo("");
                assertThat(result.get().getSggNm()).isEqualTo("");
                assertThat(result.get().getSellerId()).isEqualTo(csvInfoList.get(0).getSellerId());
                assertThat(result.get().getBizNo()).isEqualTo(csvInfoList.get(0).getBizNo());
                assertThat(result.get().getBizNm()).isEqualTo(csvInfoList.get(0).getBizNm());
        }

        @Test
        @DisplayName("API 호출 중 예외 발생 시 빈 Optional 반환")
        void processAsync_exception_in_api_call() throws Exception {
                // given
                when(corpApiClient.getCorpRegNo(anyString()))
                        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Corp API error")));

                when(regionApiClient.getRegionInfo(anyString()))
                        .thenReturn(CompletableFuture.completedFuture(RegionInfoDto.builder()
                                .regionCd("1168010300")
                                .siNm("서울특별시")
                                .sggNm("강남구")
                                .build()));

                // when
                CompletableFuture<Optional<CorpMastCreateDTO>> future = coSellerService
                        .processAsync(csvInfoList.get(0), "testuser");
                Optional<CorpMastCreateDTO> result = future.get();

                // then
                assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("saveCoSeller: 중복 bizNo 처리 검증")
        void saveCoSeller_handles_duplicate_bizNo() {
                // given
                List<BizCsvInfoDto> testCsvList = new ArrayList<>();
                testCsvList.add(BizCsvInfoDto.builder().sellerId("id1").bizNo("123-45-67890").bizNm("Org1").bizAddress("Addr1").build());
                testCsvList.add(BizCsvInfoDto.builder().sellerId("id2").bizNo("987-65-43210").bizNm("Org2").bizAddress("Addr2").build());
                testCsvList.add(BizCsvInfoDto.builder().sellerId("id3").bizNo("123-45-67890").bizNm("Org1_Duplicate").bizAddress("Addr1_Dup").build()); // Duplicate

                when(csvService.readBizCsv(anyString(), anyString())).thenReturn(testCsvList);

                when(corpApiClient.getCorpRegNo(anyString())).thenReturn(CompletableFuture.completedFuture("CorpRegNo"));
                when(regionApiClient.getRegionInfo(anyString())).thenReturn(CompletableFuture.completedFuture(RegionInfoDto.builder().regionCd("R1").siNm("S1").sggNm("SG1").build()));

                when(corpMastStore.findByBizNo("123-45-67890"))
                        .thenReturn(Optional.empty())
                        .thenReturn(Optional.of(mock(CorpMast.class)));
                when(corpMastStore.findByBizNo("987-65-43210")).thenReturn(Optional.empty());

                doNothing().when(corpMastStore).save(any(CorpMast.class));

                // when
                int savedCount = coSellerService.saveCoSeller(requestDto, "testuser");

                // then
                assertThat(savedCount).isEqualTo(2);
                verify(corpMastStore, times(2)).save(any(CorpMast.class));
                verify(corpMastHistoryStore, times(3)).save(any());
        }

        @Test
        @DisplayName("saveCoSeller: DataIntegrityViolationException 처리 검증")
        void saveCoSeller_handles_dataIntegrityViolationException() {
                // given
                List<BizCsvInfoDto> testCsvList = List.of(
                        BizCsvInfoDto.builder().sellerId("id1").bizNo("123-45-67890").bizNm("Org1").bizAddress("Addr1").build()
                );
                when(csvService.readBizCsv(anyString(), anyString())).thenReturn(testCsvList);
                when(corpApiClient.getCorpRegNo(anyString())).thenReturn(CompletableFuture.completedFuture("CorpRegNo"));
                when(regionApiClient.getRegionInfo(anyString())).thenReturn(CompletableFuture.completedFuture(RegionInfoDto.builder().regionCd("R1").siNm("S1").sggNm("SG1").build()));

                when(corpMastStore.findByBizNo(anyString())).thenReturn(Optional.empty());

                doThrow(new DataIntegrityViolationException("Simulated unique constraint violation"))
                        .when(corpMastStore).save(any(CorpMast.class));

                // when
                int savedCount = coSellerService.saveCoSeller(requestDto, "testuser");

                // then
                assertThat(savedCount).isEqualTo(0);
                verify(corpMastStore, times(1)).save(any(CorpMast.class));
                verify(corpMastHistoryStore, times(1)).save(any());
        }

        @Test
        @DisplayName("saveCoSeller: 일반 예외 처리 검증")
        void saveCoSeller_handles_general_exception() {
                // given
                List<BizCsvInfoDto> testCsvList = List.of(
                        BizCsvInfoDto.builder().sellerId("id1").bizNo("123-45-67890").bizNm("Org1").bizAddress("Addr1").build()
                );
                when(csvService.readBizCsv(anyString(), anyString())).thenReturn(testCsvList);
                when(corpApiClient.getCorpRegNo(anyString())).thenReturn(CompletableFuture.completedFuture("CorpRegNo"));
                when(regionApiClient.getRegionInfo(anyString())).thenReturn(CompletableFuture.completedFuture(RegionInfoDto.builder().regionCd("R1").siNm("S1").sggNm("SG1").build()));

                when(corpMastStore.findByBizNo(anyString())).thenReturn(Optional.empty());

                doThrow(new RuntimeException("Simulated unexpected error"))
                        .when(corpMastStore).save(any(CorpMast.class));

                // when
                int savedCount = coSellerService.saveCoSeller(requestDto, "testuser");

                // then
                assertThat(savedCount).isEqualTo(0); // 아무것도 저장되지 않음
                verify(corpMastStore, times(1)).save(any(CorpMast.class));
                verify(corpMastHistoryStore, times(1)).save(any());
        }
}