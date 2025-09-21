package com.antock.global.common.exception;

import com.antock.global.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(BusinessException.class)
        protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
                log.error("Business exception occurred: {}", e.getMessage(), e);

                ApiResponse<Void> response = ApiResponse.of(
                                e.getErrorCode().getHttpStatus(),
                                e.getMessage(),
                                null);

                return ResponseEntity
                                .status(e.getErrorCode().getHttpStatus())
                                .body(response);
        }

        @ExceptionHandler(CustomException.class)
        public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
                log.debug("Custom exception: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.of(
                                ex.getHttpStatus(),
                                ex.getMessage(),
                                null);

                return ResponseEntity
                                .status(ex.getHttpStatus())
                                .body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException e) {
                log.error("Validation error occurred: {}", e.getMessage());

                String errorMessage = e.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                .collect(Collectors.joining(", "));

                if (errorMessage.isEmpty()) {
                        errorMessage = "입력값이 올바르지 않습니다.";
                }

                ApiResponse<Void> response = ApiResponse.of(
                                HttpStatus.BAD_REQUEST,
                                errorMessage,
                                null);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }

        @ExceptionHandler(NoResourceFoundException.class)
        protected ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
                // 정적 리소스 요청은 디버그 레벨로만 로깅
                String resourcePath = e.getMessage();
                if (resourcePath.contains("favicon.ico") ||
                                resourcePath.contains(".well-known") ||
                                resourcePath.contains("chrome.devtools")) {
                        log.debug("Static resource not found: {}", resourcePath);
                } else {
                        log.warn("Resource not found: {}", resourcePath);
                }

                ApiResponse<Void> response = ApiResponse.of(
                                HttpStatus.NOT_FOUND,
                                "요청한 리소스를 찾을 수 없습니다.",
                                null);

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(response);
        }

        @ExceptionHandler(Exception.class)
        protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
                if (e instanceof NoResourceFoundException) {
                        return handleNoResourceFoundException((NoResourceFoundException) e);
                }

                log.error("Unexpected error occurred: {}", e.getMessage(), e);

                ApiResponse<Void> response = ApiResponse.of(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "서버 내부 오류가 발생했습니다.",
                                null);

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(response);
        }
}