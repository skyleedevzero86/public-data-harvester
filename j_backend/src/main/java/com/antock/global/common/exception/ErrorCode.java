package com.antock.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "엔티티를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 오류가 발생했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "접근이 거부되었습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "M002", "이미 사용중인 사용자명입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M003", "이미 사용중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "M004", "아이디 또는 비밀번호가 잘못되었습니다."),
    MEMBER_NOT_APPROVED(HttpStatus.FORBIDDEN, "M005", "승인되지 않은 회원입니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "M006", "계정이 잠겨있습니다."),
    INVALID_MEMBER_STATUS(HttpStatus.FORBIDDEN, "M007", "유효하지 않은 회원 상태입니다."),

    // Security
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "S001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "S002", "만료된 토큰입니다."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "S003", "요청 한도를 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}