package com.antock.member.presentation;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.presentation.MemberApiController;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberApiController.class)
class MemberApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberApplicationService memberApplicationService;

    @Test
    @DisplayName("회원가입 API 성공")
    void join_Success() throws Exception {
        MemberJoinRequest request = MemberJoinRequest.builder()
                .username("testuser")
                .password("Test1234!@")
                .nickname("테스트")
                .email("test@example.com")
                .build();

        MemberResponse response = MemberResponse.builder()
                .id(1L)
                .username("testuser")
                .nickname("테스트")
                .email("test@example.com")
                .status(MemberStatus.PENDING)
                .role(Role.USER)
                .createDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .build();

        given(memberApplicationService.join(any(MemberJoinRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value(201))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("회원가입 API 유효성 검증 실패")
    void join_ValidationFailed() throws Exception {
        MemberJoinRequest request = MemberJoinRequest.builder()
                .username("")
                .password("123")
                .nickname("")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}