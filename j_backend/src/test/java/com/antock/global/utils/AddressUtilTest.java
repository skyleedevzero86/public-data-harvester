package com.antock.global.utils;

import com.antock.global.utils.AddressUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressUtilTest {

    @Test
    @DisplayName("주소 읍/면/동 추출 검증")
    public void extract_address() throws Exception {
        //given
        String address = "서울특별시 강남구 개포동 112-123";

        // when
        String result = AddressUtil.extractAddress(address);

        // then
        assertThat(result).isEqualTo("서울특별시 강남구 개포동");
    }

    @Test
    @DisplayName("빈 주소 전달시 오류 검증")
    public void exception_empty_address() throws Exception {

        //when
        String result = AddressUtil.extractAddress("");
        //then
        assertThat(result).isEqualTo("");

    }

    @Test
    @DisplayName("읍/면/동 정보 없는경우 검증")
    public void less_token_address () throws Exception {

        //when
        String result = AddressUtil.extractAddress("서울특별시 강남구");
        //then
        assertThat(result).isEqualTo("서울특별시 강남구");

    }
}
