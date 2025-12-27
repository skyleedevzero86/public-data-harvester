package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoSellerStorageService 테스트")
class CoSellerStorageServiceTest {

    @Mock
    private CorpMastStore corpMastStore;

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
        given(corpMastStore.findExistingBizNos(anyList())).willReturn(List.of());

        int savedCount = coSellerStorageService.saveCorpMastList(dtos, "testuser");

        assertThat(savedCount).isGreaterThanOrEqualTo(0);
        verify(corpMastStore).findExistingBizNos(anyList());
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

