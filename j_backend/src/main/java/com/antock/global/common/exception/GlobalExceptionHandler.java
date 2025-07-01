package com.antock.global.common.exception;

import com.antock.global.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //Enum 매핑 실패시 Exception handler
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleInvalidEnum(MethodArgumentNotValidException ex) {  // @ValidEnum에서 설정한 메시지 처리

        String errorMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)  // 메시지 출력
                .collect(Collectors.joining(", "));

        log.debug("검증 실패: {}", errorMsg);

        return ApiResponse.of(HttpStatus.BAD_REQUEST,errorMsg,"");

    }

    //커스텀 예외 발생시 Exception handler
    @ExceptionHandler(CustomException.class)
    public ApiResponse<?> handleCustomException(CustomException ex) {
        log.debug (ex.getResultMsg());
        return ApiResponse.of(ex.getHttpStatus(), ex.getResultMsg());
    }
}