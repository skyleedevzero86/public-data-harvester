package com.antock.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

        @Value("${server.port:8080}")
        private String serverPort;

        @Value("${spring.profiles.active:local}")
        private String activeProfile;

        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                                .info(apiInfo())
                                .servers(createServers())
                                .security(createSecurityRequirements())
                                .components(createComponents())
                                .tags(createTags())
                                .externalDocs(createExternalDocumentation());
        }

        private Info apiInfo() {
                return new Info()
                                .title("Antock Public Data Harvester API")
                                .description("""
                                                ## 개요
                                                Antock 공공데이터 수집 및 관리 시스템의 RESTful API 문서입니다.

                                                ## 인증 방식
                                                - **JWT Bearer Token**: Authorization 헤더에 'Bearer {token}' 형식으로 전송
                                                - **API Key**: X-API-Key 헤더에 API 키 전송 (일부 엔드포인트)

                                                ## 주요 기능 영역

                                                ### 1. 관리자 기능 (Admin)
                                                - 시스템 메트릭 조회 및 모니터링
                                                - 캐시 관리 (Redis/Memory)
                                                - 속도 제한 (Rate Limiting) 관리
                                                - 보안 화이트리스트/블랙리스트 관리
                                                - 시스템 헬스체크

                                                ### 2. 법인 정보 관리 (CorpMast)
                                                - 법인 정보 등록, 수정, 삭제
                                                - 법인 정보 검색 (다양한 조건)
                                                - 사업자번호/법인등록번호로 조회
                                                - Excel 파일로 내보내기
                                                - 지역별 법인 통계

                                                ### 3. 회원 관리 (Member)
                                                - 회원 가입 및 로그인
                                                - 프로필 관리
                                                - 비밀번호 변경 및 보안 정책
                                                - 관리자의 회원 승인/거부
                                                - 역할 기반 권한 관리 (USER/MANAGER/ADMIN)

                                                ### 4. 파일 관리 (File)
                                                - 파일 업로드 (Local/MinIO)
                                                - 파일 다운로드
                                                - 파일 메타데이터 관리
                                                - 파일 검색 및 필터링

                                                ### 5. 대시보드 및 통계 (Dashboard)
                                                - 지역별 통계 정보
                                                - 시스템 현황 요약
                                                - 최근 활동 내역

                                                ### 6. 공동판매자 관리 (CoSeller)
                                                - CSV 파일 기반 대량 데이터 처리
                                                - 외부 API 연동 (법인정보, 지역정보)
                                                - 배치 작업 스케줄링

                                                ## 에러 코드

                                                ### HTTP 상태 코드
                                                - `200 OK`: 요청 성공
                                                - `201 Created`: 리소스 생성 성공
                                                - `400 Bad Request`: 잘못된 요청
                                                - `401 Unauthorized`: 인증 실패
                                                - `403 Forbidden`: 권한 없음
                                                - `404 Not Found`: 리소스를 찾을 수 없음
                                                - `409 Conflict`: 리소스 충돌 (중복 등)
                                                - `429 Too Many Requests`: 요청 한도 초과
                                                - `500 Internal Server Error`: 서버 내부 오류

                                                ### 비즈니스 에러 코드
                                                - `C00X`: 공통 에러
                                                - `M00X`: 회원 관련 에러
                                                - `S00X`: 보안 관련 에러
                                                - `P00X`: 비밀번호 관련 에러
                                                - `V00X`: 검증 관련 에러
                                                - `E00X`: 외부 API 에러
                                                - `CP0XX`: 법인 관련 에러
                                                - `F00X`: 파일 관련 에러
                                                - `CSV0XX`: CSV 처리 에러

                                                ## 페이징
                                                모든 리스트 조회 API는 페이징을 지원합니다.
                                                - `page`: 페이지 번호 (0부터 시작)
                                                - `size`: 페이지 크기 (최대 100)
                                                - `sort`: 정렬 조건 (예: id,desc)

                                                ## 캐싱
                                                주요 조회 API는 Redis 또는 메모리 캐싱을 지원합니다.
                                                - 캐시 TTL: 5분 (기본값)
                                                - 관리자 API를 통한 캐시 무효화 가능

                                                ## 속도 제한
                                                API 호출에는 속도 제한이 적용됩니다.
                                                - 기본: 분당 60회
                                                - 버스트: 100회
                                                - 위반 시 자동 차단 (30분)
                                                """)
                                .version("1.0.0")
                                .contact(new Contact()
                                                .name("Antock Development Team")
                                                .email("dev@antock.com")
                                                .url("https://github.com/antock"))
                                .license(new License()
                                                .name("MIT License")
                                                .url("https://opensource.org/licenses/MIT"));
        }

        private List<Server> createServers() {
                return Arrays.asList(
                                new Server()
                                                .url("http://localhost:" + serverPort)
                                                .description("로컬 개발 서버"),
                                new Server()
                                                .url("https://dev-api.antock.com")
                                                .description("개발 서버"),
                                new Server()
                                                .url("https://api.antock.com")
                                                .description("운영 서버"));
        }

        private List<SecurityRequirement> createSecurityRequirements() {
                return Arrays.asList(
                                new SecurityRequirement().addList("Bearer Authentication"),
                                new SecurityRequirement().addList("API Key Authentication"));
        }

        private Components createComponents() {
                return new Components()
                                .addSecuritySchemes("Bearer Authentication", createJwtSecurityScheme())
                                .addSecuritySchemes("API Key Authentication", createApiKeySecurityScheme())
                                .addParameters("pageParam", createPageParameter())
                                .addParameters("sizeParam", createSizeParameter())
                                .addParameters("sortParam", createSortParameter())
                                .addResponses("BadRequest", createBadRequestResponse())
                                .addResponses("Unauthorized", createUnauthorizedResponse())
                                .addResponses("Forbidden", createForbiddenResponse())
                                .addResponses("NotFound", createNotFoundResponse())
                                .addResponses("Conflict", createConflictResponse())
                                .addResponses("TooManyRequests", createTooManyRequestsResponse())
                                .addResponses("InternalServerError", createInternalServerErrorResponse())
                                .addSchemas("ErrorResponse", createErrorResponseSchema())
                                .addSchemas("PageInfo", createPageInfoSchema())
                                .addSchemas("SortInfo", createSortInfoSchema());
        }

        private List<Tag> createTags() {
                return Arrays.asList(
                                new Tag()
                                                .name("Admin - System Metrics")
                                                .description("시스템 메트릭 및 성능 모니터링 API"),
                                new Tag()
                                                .name("Admin - Cache Management")
                                                .description("캐시 모니터링 및 관리 API"),
                                new Tag()
                                                .name("Admin - Health Check")
                                                .description("시스템 헬스체크 API"),
                                new Tag()
                                                .name("CorpMast Management")
                                                .description("법인 정보 관리 API"),
                                new Tag()
                                                .name("Member Management")
                                                .description("회원 관리 및 인증 API"),
                                new Tag()
                                                .name("File Management")
                                                .description("파일 업로드/다운로드 관리 API"),
                                new Tag()
                                                .name("Dashboard")
                                                .description("대시보드 및 통계 API"),
                                new Tag()
                                                .name("CoSeller Management")
                                                .description("공동판매자 및 CSV 배치 처리 API"),
                                new Tag()
                                                .name("Region Statistics")
                                                .description("지역별 통계 API"));
        }

        private ExternalDocumentation createExternalDocumentation() {
                return new ExternalDocumentation()
                                .description("프로젝트 GitHub 저장소")
                                .url("https://github.com/antock/public-data-harvester");
        }

        private SecurityScheme createJwtSecurityScheme() {
                return new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT 토큰 기반 인증. Authorization 헤더에 'Bearer {token}' 형식으로 전송");
        }

        private SecurityScheme createApiKeySecurityScheme() {
                return new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("API 키 기반 인증. X-API-Key 헤더에 API 키를 전송");
        }

        private Parameter createPageParameter() {
                return new Parameter()
                                .name("page")
                                .description("페이지 번호 (0부터 시작)")
                                .required(false)
                                .schema(new Schema<Integer>().type("integer").minimum(0)._default(0));
        }

        private Parameter createSizeParameter() {
                return new Parameter()
                                .name("size")
                                .description("페이지 크기 (최대 100)")
                                .required(false)
                                .schema(new Schema<Integer>().type("integer").minimum(1).maximum(100)._default(20));
        }

        private Parameter createSortParameter() {
                return new Parameter()
                                .name("sort")
                                .description("정렬 조건 (예: id,desc 또는 createDate,asc)")
                                .required(false)
                                .schema(new StringSchema()._default("id,desc"));
        }

        private ApiResponse createBadRequestResponse() {
                return new ApiResponse()
                                .description("잘못된 요청 - 요청 파라미터나 본문이 올바르지 않음");
        }

        private ApiResponse createUnauthorizedResponse() {
                return new ApiResponse()
                                .description("인증 실패 - 유효하지 않은 토큰이거나 토큰이 만료됨");
        }

        private ApiResponse createForbiddenResponse() {
                return new ApiResponse()
                                .description("권한 없음 - 해당 리소스에 접근할 권한이 없음");
        }

        private ApiResponse createNotFoundResponse() {
                return new ApiResponse()
                                .description("리소스를 찾을 수 없음");
        }

        private ApiResponse createConflictResponse() {
                return new ApiResponse()
                                .description("리소스 충돌 - 중복된 데이터나 상태 충돌");
        }

        private ApiResponse createTooManyRequestsResponse() {
                return new ApiResponse()
                                .description("요청 한도 초과 - 속도 제한에 걸림");
        }

        private ApiResponse createInternalServerErrorResponse() {
                return new ApiResponse()
                                .description("서버 내부 오류");
        }

        private Schema<?> createErrorResponseSchema() {
                return new Schema<>()
                                .type("object")
                                .description("에러 응답 모델")
                                .addProperty("success",
                                                new Schema<Boolean>().type("boolean").description("성공 여부")
                                                                .example(false))
                                .addProperty("message",
                                                new StringSchema().description("에러 메시지").example("요청 처리 중 오류가 발생했습니다"))
                                .addProperty("data",
                                                new Schema<>().type("object").nullable(true).description("에러 상세 정보"))
                                .addProperty("timestamp", new StringSchema().format("date-time").description("응답 시간"));
        }

        private Schema<?> createPageInfoSchema() {
                return new Schema<>()
                                .type("object")
                                .description("페이징 정보")
                                .addProperty("size",
                                                new Schema<Integer>().type("integer").description("페이지 크기").example(20))
                                .addProperty("number",
                                                new Schema<Integer>().type("integer").description("현재 페이지 번호")
                                                                .example(0))
                                .addProperty("totalElements",
                                                new Schema<Long>().type("integer").format("int64")
                                                                .description("전체 요소 수").example(150))
                                .addProperty("totalPages",
                                                new Schema<Integer>().type("integer").description("전체 페이지 수")
                                                                .example(8))
                                .addProperty("first",
                                                new Schema<Boolean>().type("boolean").description("첫 페이지 여부")
                                                                .example(true))
                                .addProperty("last",
                                                new Schema<Boolean>().type("boolean").description("마지막 페이지 여부")
                                                                .example(false))
                                .addProperty("hasNext",
                                                new Schema<Boolean>().type("boolean").description("다음 페이지 존재 여부")
                                                                .example(true))
                                .addProperty("hasPrevious", new Schema<Boolean>().type("boolean")
                                                .description("이전 페이지 존재 여부").example(false));
        }

        private Schema<?> createSortInfoSchema() {
                return new Schema<>()
                                .type("object")
                                .description("정렬 정보")
                                .addProperty("sorted",
                                                new Schema<Boolean>().type("boolean").description("정렬 적용 여부")
                                                                .example(true))
                                .addProperty("unsorted",
                                                new Schema<Boolean>().type("boolean").description("정렬 미적용 여부")
                                                                .example(false))
                                .addProperty("empty", new Schema<Boolean>().type("boolean").description("정렬 조건 비어있음 여부")
                                                .example(false));
        }
}
