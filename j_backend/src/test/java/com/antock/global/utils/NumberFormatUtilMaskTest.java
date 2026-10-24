package com.antock.global.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NumberFormatUtilMaskTest {

    @Test
    @DisplayName("사업자번호와 법인등록번호의 마지막 숫자만 가린다")
    void maskLastDigit_hidesOnlyTheLastDigit() {
        assertThat(NumberFormatUtil.maskLastDigit("124-81-00998")).isEqualTo("124-81-0099*");
        assertThat(NumberFormatUtil.maskLastDigit("110111-1234567")).isEqualTo("110111-123456*");
        assertThat(NumberFormatUtil.maskLastDigit("")).isEqualTo("");
        assertThat(NumberFormatUtil.maskLastDigit(null)).isEqualTo("");
    }
}
