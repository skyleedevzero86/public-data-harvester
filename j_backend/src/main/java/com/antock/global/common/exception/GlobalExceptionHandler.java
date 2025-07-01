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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleInvalidEnum(MethodArgumentNotValidException ex) {  // @ValidEnum에서 설정한 메시지 처리

        String errorMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)  // 메시지 출력
                .collect(Collectors.joining(", "));

        log.warn("검증 실패: {}", errorMsg);

        return ApiResponse.of(HttpStatus.BAD_REQUEST,errorMsg,"");

    }
}