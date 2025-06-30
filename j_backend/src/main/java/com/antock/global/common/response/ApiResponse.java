package com.antock.global.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * API응답 공통 처리
 * @param <T>
 */

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int resultCode;
    private String resultMsg;
    private T data; //응답데이터

    public ApiResponse(HttpStatus resultStatus, String resultMsg) {
        this.resultCode = resultStatus.value();
        this.resultMsg = resultMsg;
    }

    public ApiResponse(HttpStatus resultStatus, String resultMsg, T data) {
        this.resultCode = resultStatus.value();
        this.resultMsg = resultMsg;
        this.data = data;
    }

    public static <T> ApiResponse<T> of(HttpStatus resultStatus, T data) {
        return new ApiResponse<>(resultStatus, resultStatus.getReasonPhrase(), data);
    }



}
