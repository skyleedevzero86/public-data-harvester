package com.antock.global.utils;

import com.antock.global.utils.AddressUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressUtilTest {

    @Test
    @DisplayName("주소 읍/면/동 추출 검증")
    public void extract_address() throws Exception {
        String address = "서울특별시 강남구 개포동 112-123";

        String result = AddressUtil.extractAddress(address);

        assertThat(result).isEqualTo("서울특별시 강남구 개포동");
    }

    @Test
    @DisplayName("빈 주소 전달시 오류 검증")
    public void exception_empty_address() throws Exception {

        String result = AddressUtil.extractAddress("");
        assertThat(result).isEqualTo("");

    }

    @Test
    @DisplayName("읍/면/동 정보 없는경우 검증")
    public void less_token_address () throws Exception {

        String result = AddressUtil.extractAddress("서울특별시 강남구");
        assertThat(result).isEqualTo("서울특별시 강남구");

    }
}
