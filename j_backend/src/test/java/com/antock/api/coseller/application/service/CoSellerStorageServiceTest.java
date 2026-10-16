package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastHistoryStore;
import com.antock.api.coseller.infrastructure.CorpMastStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoSellerStorageService 테스트")
class CoSellerStorageServiceTest {

    @Mock
    private CorpMastStore corpMastStore;

    @Mock
    private CorpMastHistoryStore corpMastHistoryStore;

    @Mock
    private Executor asyncExecutor;

    @InjectMocks
    private CoSellerStorageService coSellerStorageService;

    @Test
    @DisplayName("법인 정보 저장 성공")
    void saveCorpMastList_Success() {
        CorpMastCreateDTO dto1 = CorpMastCreateDTO.builder()
                .sellerId("seller001")
                .bizNm("법인1")
                .bizNo("123-45-67890")
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();

        CorpMastCreateDTO dto2 = CorpMastCreateDTO.builder()
                .sellerId("seller002")
                .bizNm("법인2")
                .bizNo("123-45-67891")
                .corpRegNo("110111-1234568")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("서초구")
                .username("testuser")
                .build();

        List<CorpMastCreateDTO> dtos = Arrays.asList(dto1, dto2);
        given(corpMastStore.findExistingBizNos(anyList())).willReturn(List.of("123-45-67890"));
        given(corpMastStore.upsertAll(anyList())).willReturn(2);

        int savedCount = coSellerStorageService.saveCorpMastList(dtos, "testuser");

        assertThat(savedCount).isEqualTo(2);
        ArgumentCaptor<List<CorpMast>> saved = ArgumentCaptor.forClass(List.class);
        verify(corpMastStore).upsertAll(saved.capture());
        assertThat(saved.getValue()).extracting(CorpMast::getBizNo)
                .containsExactly("123-45-67890", "123-45-67891");
        verify(corpMastStore, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("같은 사업자번호는 마지막 행으로 한 번만 갱신한다")
    void saveCorpMastList_duplicateBizNoKeepsLastRow() {
        CorpMastCreateDTO first = CorpMastCreateDTO.builder()
                .sellerId("seller001")
                .bizNm("이전상호")
                .bizNo("123-45-67890")
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();
        CorpMastCreateDTO latest = CorpMastCreateDTO.builder()
                .sellerId("seller001")
                .bizNm("변경상호")
                .bizNo("123-45-67890")
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("서초구")
                .username("testuser")
                .build();
        given(corpMastStore.findExistingBizNos(anyList())).willReturn(List.of("123-45-67890"));
        given(corpMastStore.upsertAll(anyList())).willReturn(1);

        int savedCount = coSellerStorageService.saveCorpMastList(List.of(first, latest), "testuser");

        assertThat(savedCount).isEqualTo(1);
        ArgumentCaptor<List<CorpMast>> saved = ArgumentCaptor.forClass(List.class);
        verify(corpMastStore).upsertAll(saved.capture());
        assertThat(saved.getValue()).hasSize(1);
        assertThat(saved.getValue().get(0).getBizNm()).isEqualTo("변경상호");
        assertThat(saved.getValue().get(0).getSggNm()).isEqualTo("서초구");
    }

    @Test
    @DisplayName("법인 정보 저장 - 빈 리스트")
    void saveCorpMastList_EmptyList() {
        List<CorpMastCreateDTO> emptyList = List.of();

        int savedCount = coSellerStorageService.saveCorpMastList(emptyList, "testuser");

        assertThat(savedCount).isEqualTo(0);
    }

    @Test
    @DisplayName("전체 데이터 삭제 성공")
    void clearAllData_Success() {
        CorpMast corp1 = CorpMast.builder()
                .sellerId("seller001")
                .bizNm("법인1")
                .bizNo("123-45-67890")
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();

        List<CorpMast> allCorps = Arrays.asList(corp1);
        given(corpMastStore.findAll()).willReturn(allCorps);

        int deletedCount = coSellerStorageService.clearAllData();

        assertThat(deletedCount).isEqualTo(1);
        verify(corpMastStore).findAll();
    }
}

