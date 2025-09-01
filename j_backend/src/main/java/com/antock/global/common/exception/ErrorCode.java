package com.antock.global.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "요청한 데이터를 찾을 수 없습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 메서드입니다."),
    COMMON_ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "엔티티를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 오류가 발생했습니다."),
    COMMON_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "접근이 거부되었습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "C007", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C008", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C009", "접근 권한이 없습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "C010", "입력 파라미터가 올바르지 않습니다."),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "E0004", "접근 권한이 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E0005", "잘못된 요청입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "M002", "이미 사용중인 사용자명입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M003", "이미 사용중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "M004", "아이디 또는 비밀번호가 잘못되었습니다."),
    MEMBER_NOT_APPROVED(HttpStatus.FORBIDDEN, "M005", "승인되지 않은 회원입니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "M006", "계정이 잠겨있습니다."),
    INVALID_MEMBER_STATUS(HttpStatus.FORBIDDEN, "M007", "유효하지 않은 회원 상태입니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "S001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "S002", "만료된 토큰입니다."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "S003", "요청 한도를 초과했습니다."),
    IDENTIFIER_BLOCKED(HttpStatus.FORBIDDEN, "S004", "차단된 식별자입니다."),
    SECURITY_VIOLATION(HttpStatus.FORBIDDEN, "S005", "보안 정책 위반이 감지되었습니다."),

    INVALID_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "P001", "현재 비밀번호가 일치하지 않습니다."),
    PASSWORD_CONFIRMATION_MISMATCH(HttpStatus.BAD_REQUEST, "P002", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "P003", "새 비밀번호는 현재 비밀번호와 다르게 설정해주세요."),
    RECENTLY_USED_PASSWORD(HttpStatus.BAD_REQUEST, "P004", "최근에 사용한 비밀번호는 재사용할 수 없습니다."),
    DAILY_PASSWORD_CHANGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "P005", "일일 비밀번호 변경 한도를 초과했습니다. 내일 다시 시도해주세요."),
    PASSWORD_CHANGE_REQUIRED(HttpStatus.FORBIDDEN, "MEMBER_4006", "비밀번호 변경이 필요합니다."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER_4007", "비밀번호가 보안 정책을 만족하지 않습니다."),

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "V001", "데이터 검증에 실패했습니다."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "V002", "필수 입력 항목이 누락되었습니다."),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "V003", "데이터 형식이 올바르지 않습니다."),

    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "E001", "외부 API 호출 중 오류가 발생했습니다."),
    API_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "E002", "API 응답 시간이 초과되었습니다."),

    CORP_NOT_FOUND(HttpStatus.NOT_FOUND, "CP001", "법인 정보를 찾을 수 없습니다."),
    CORP_ALREADY_EXISTS(HttpStatus.CONFLICT, "CP002", "이미 등록된 법인입니다."),
    INVALID_BIZ_NO(HttpStatus.BAD_REQUEST, "CP003", "사업자번호 형식이 올바르지 않습니다."),
    CORP_SEARCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CP005", "법인 검색 중 오류가 발생했습니다."),
    INVALID_CORP_REG_NO(HttpStatus.BAD_REQUEST, "CP004", "법인등록번호 형식이 올바르지 않습니다."),

    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F002", "파일 업로드에 실패했습니다."),
    FILE_DOWNLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F003", "파일 다운로드에 실패했습니다."),
    FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F004", "파일 삭제에 실패했습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F005", "파일 크기가 제한을 초과했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F006", "허용되지 않는 파일 형식입니다."),
    FILE_NAME_INVALID(HttpStatus.BAD_REQUEST, "F007", "파일명이 유효하지 않습니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "F008", "파일이 비어있습니다."),
    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F009", "파일 저장소 오류가 발생했습니다."),
    FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "F010", "파일에 대한 접근 권한이 없습니다."),

    CSV_PARSING_ERROR(HttpStatus.BAD_REQUEST, "CSV001", "CSV 파일 파싱 중 오류가 발생했습니다."),
    CSV_FORMAT_ERROR(HttpStatus.BAD_REQUEST, "CSV002", "CSV 파일 형식이 올바르지 않습니다."),
    CSV_EMPTY_FILE(HttpStatus.BAD_REQUEST, "CSV003", "CSV 파일이 비어있습니다."),
    CSV_INVALID_HEADER(HttpStatus.BAD_REQUEST, "CSV004", "CSV 파일의 헤더가 올바르지 않습니다."),
    CSV_DATA_ERROR(HttpStatus.BAD_REQUEST, "CSV005", "CSV 데이터에 오류가 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    private ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}