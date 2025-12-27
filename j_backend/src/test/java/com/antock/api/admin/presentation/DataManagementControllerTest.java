package com.antock.api.admin.presentation;

import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.config.DataInitProperties;
import com.antock.global.config.MemberDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataManagementController.class)
@DisplayName("DataManagementController 테스트")
class DataManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private MemberDataGenerator memberDataGenerator;

    @MockBean
    private DataInitProperties dataInitProperties;

    @Test
    @DisplayName("회원 데이터 통계 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getMemberStatistics_Success() throws Exception {
        given(memberRepository.count()).willReturn(100L);
        given(memberRepository.countByRole(any(Role.class))).willReturn(50L);
        given(memberRepository.countByStatus(any(MemberStatus.class))).willReturn(50L);
        given(memberRepository.countByRoleAndStatus(any(Role.class), any(MemberStatus.class))).willReturn(25L);

        mockMvc.perform(get("/api/admin/data/members/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(100));
    }

    @Test
    @DisplayName("더미 회원 데이터 생성 성공")
    @WithMockUser(roles = "ADMIN")
    void generateMemberData_Success() throws Exception {
        given(memberRepository.count()).willReturn(0L, 100L);
        given(dataInitProperties.isForceInit()).willReturn(false);

        mockMvc.perform(post("/api/admin/data/members/generate")
                        .with(csrf())
                        .param("count", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("더미 회원 데이터 생성 - 잘못된 개수")
    @WithMockUser(roles = "ADMIN")
    void generateMemberData_InvalidCount() throws Exception {
        mockMvc.perform(post("/api/admin/data/members/generate")
                        .with(csrf())
                        .param("count", "10001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("데이터 초기화 설정 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getDataConfig_Success() throws Exception {
        given(dataInitProperties.isEnabled()).willReturn(true);
        given(dataInitProperties.getMemberCount()).willReturn(1000);
        given(dataInitProperties.isForceInit()).willReturn(false);
        given(dataInitProperties.getBatchSize()).willReturn(100);

        mockMvc.perform(get("/api/admin/data/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    @DisplayName("데이터 초기화 설정 업데이트 성공")
    @WithMockUser(roles = "ADMIN")
    void updateDataConfig_Success() throws Exception {
        given(dataInitProperties.isEnabled()).willReturn(true);
        given(dataInitProperties.getMemberCount()).willReturn(2000);
        given(dataInitProperties.isForceInit()).willReturn(true);
        given(dataInitProperties.getBatchSize()).willReturn(200);

        mockMvc.perform(put("/api/admin/data/config")
                        .with(csrf())
                        .param("enabled", "true")
                        .param("memberCount", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

