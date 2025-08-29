![image](https://github.com/user-attachments/assets/0ab4c671-8dfc-4498-8f6d-47b3b9d5ab4d)
![image](https://github.com/user-attachments/assets/68edacdd-3ccb-4f2c-8371-a2cca89aca93)


# 🍀 프로젝트 소개 –  Antock Public Data

공공데이터 수집 및 관리 시스템의 백엔드 애플리케이션입니다.

## 🚀 주요 기능

- **법인 정보 관리**: 법인 등록, 수정, 삭제, 검색
- **회원 관리**: 회원가입, 로그인, 권한 관리
- **파일 관리**: 파일 업로드/다운로드, MinIO/Local 저장소 지원
- **CSV 배치 처리**: 대량 데이터 처리 및 내보내기
- **대시보드**: 지역별 통계 및 성능 모니터링
- **캐시 관리**: Redis/Memory 기반 캐시 시스템
- **보안**: JWT 기반 인증, Rate Limiting

## 🗃️ 문서 목록
### ERD
![image](https://github.com/skyleedevzero86/public-data-harvester/blob/main/j_backend/src/main/resources/docs/img.png)

- [기능 요구사항](https://github.com/skyleedevzero86/public-data-harvester/blob/main/j_backend/src/main/resources/docs/FRS.md)
- [유스케이스 시나리오](https://github.com/skyleedevzero86/public-data-harvester/blob/main/j_backend/src/main/resources/docs/UsecaseScenario.md)
- [다이어그램](https://github.com/skyleedevzero86/public-data-harvester/blob/main/j_backend/src/main/resources/docs/diagram/ComponentDiagram.md)



## 🛠 기술 스택

- **Java 17**
- **Spring Boot 3.4.4**
- **Spring Security**
- **Spring Data JPA**
- **Redis**
- **MinIO**
- **PostgreSQL**
- **Gradle**

## 📚 API 문서

### Swagger UI

- **URL**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/api-docs`

### API 그룹

#### 1. Admin APIs

- **Cache Monitoring**: `/api/admin/cache/**`
  - 캐시 통계 조회
  - 캐시 무효화
  - Rate Limiting 관리
- **System Metrics**: `/api/admin/metrics/**`
  - 시스템 성능 지표
  - 메모리 사용량
  - 보안 상태

#### 2. Member APIs

- **Authentication**: `/api/v1/members/**`
  - 회원가입/로그인
  - 프로필 관리
  - 비밀번호 변경
- **Admin Management**: `/api/v1/members/admin/**`
  - 회원 승인/거부
  - 역할 변경
  - 통계 조회

#### 3. Corp APIs

- **Manual Management**: `/api/v1/corp/**`
  - 법인 정보 검색
  - 법인 정보 CRUD
  - 엑셀 내보내기
  - 지역별 통계

#### 4. File APIs

- **File Management**: `/api/v1/files/**`
  - 파일 업로드/다운로드
  - 파일 메타데이터 관리
  - 저장소 전략 (Local/MinIO)

#### 5. Dashboard APIs

- **Statistics**: `/api/v1/region-stats/**`
  - 지역별 통계
  - 성과 지표

## 🔧 설정

### 환경별 설정 파일

- `application.yml`: 기본 설정
- `application-dev.yml`: 개발 환경
- `application-prod.yml`: 운영 환경

### 주요 설정 항목

```yaml
# JWT 설정
jwt:
  secret: your-secret-key
  expiration: 3600000

# Redis 설정
spring:
  redis:
    host: localhost
    port: 6379

# MinIO 설정
minio:
  endpoint: http://localhost:9000
  bucket: default-bucket

# 파일 업로드 설정
file:
  upload-dir: /tmp/uploads
  max-size: 10485760
```

## 🚀 실행 방법

### 1. 환경 요구사항

- Java 17+
- PostgreSQL 12+
- Redis 6+
- MinIO (선택사항)

### 2. 실행 명령어

```bash
# 개발 환경 (더미 데이터 자동 생성)
./gradlew bootRun --args='--spring.profiles.active=dev'

# 운영 환경
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### 3. Docker 실행

```bash
docker-compose up -d
```

### 4. 더미 데이터 자동 생성

개발 환경에서 애플리케이션 실행 시 **2000개의 테스트 계정**이 자동으로 생성됩니다:

#### 🧪 테스트 계정 정보

```bash
# 관리자 계정 (5% - 약 100개)
Username: admin0001~admin0100
Password: Admin@123!

# 매니저 계정 (15% - 약 300개)
Username: manager0001~manager0300
Password: Manager@123!

# 일반 사용자 (80% - 약 1600개)
Username: user0001~user1600
Password: User@123!
```

#### 📊 상태별 분포

- **승인됨 (70%)**: 즉시 로그인 가능한 계정
- **승인 대기 (15%)**: 관리자 승인이 필요한 계정
- **정지됨 (10%)**: 계정 잠금 상태
- **거부됨 (3%)**: 가입 거부 상태
- **탈퇴됨 (2%)**: 탈퇴 처리 상태

#### ⚙️ 더미 데이터 설정

`application-dev.yml`에서 생성 옵션을 조정할 수 있습니다:

```yaml
app:
  data:
    init:
      enabled: true # 더미 데이터 생성 활성화/비활성화
      member-count: 2000 # 생성할 회원 수
      force-init: false # 기존 데이터가 있어도 강제 생성
      batch-size: 500 # 배치 저장 크기 (성능 최적화)
```

## 📊 모니터링

### Health Check

- **Application**: `http://localhost:8080/actuator/health`
- **Cache**: `http://localhost:8080/actuator/health/memberCache`
- **Redis**: `http://localhost:8080/actuator/health/redis`

### Metrics

- **Prometheus**: `http://localhost:8080/actuator/prometheus`
- **System Metrics**: `http://localhost:8080/api/admin/metrics/**`

## 🔒 보안

### 인증

- JWT 기반 토큰 인증
- Role-based Access Control (RBAC)
- Password Policy 적용

### Rate Limiting

- Redis 기반 Rate Limiting
- IP/사용자별 제한
- 동적 차단/화이트리스트

## 📝 개발 가이드

### 코드 구조

```
src/main/java/com/antock/
├── api/                    # API 모듈
│   ├── admin/             # 관리자 기능
│   ├── corpmanual/        # 법인 정보 관리
│   ├── coseller/          # 공동판매자 관리
│   ├── csv/               # CSV 배치 처리
│   ├── dashboard/         # 대시보드
│   ├── file/              # 파일 관리
│   └── member/            # 회원 관리
├── global/                 # 공통 모듈
│   ├── common/            # 공통 클래스
│   ├── config/            # 설정
│   ├── security/          # 보안
│   └── utils/             # 유틸리티
└── web/                   # 웹 컨트롤러
```

### API 문서화 가이드

- 모든 API에 `@Operation` 어노테이션 추가
- 요청/응답 스키마 정의
- 예제 값 제공
- 에러 응답 명시

## 🧪 테스트

### 단위 테스트

```bash
./gradlew test
```

### 통합 테스트

```bash
./gradlew integrationTest
```

### API 테스트

```bash
# Swagger UI를 통한 테스트
# 또는 Postman Collection 사용
```

## 📈 성능 최적화

### 캐싱 전략

- Redis 기반 분산 캐시
- Local Memory 캐시 (Fallback)
- Cache Eviction 정책

### 배치 처리

- Chunk 기반 처리
- 비동기 실행
- 재시도 메커니즘

### 데이터베이스 최적화

- 인덱스 최적화
- N+1 쿼리 방지
- Connection Pool 설정

## 🚨 문제 해결

### 일반적인 문제

1. **Redis 연결 실패**: Memory 캐시로 자동 Fallback
2. **파일 업로드 실패**: 저장소 설정 확인
3. **JWT 토큰 만료**: Refresh Token 사용

### 로그 확인

```bash
# 애플리케이션 로그
tail -f logs/application.log

# 에러 로그
tail -f logs/error.log
```

## 📞 지원

- **개발팀**: sleekydz86@naver.com
- **문서**: [API Documentation](http://localhost:8080/swagger-ui.html)
- **이슈**: GitHub Issues



## 📌 트러블슈팅
- [Spring Security 테스트에서 커스텀 사용자 인증 객체 주입 문제 해결](https://velog.io/@sleekydevzero86/spring-security-test-with-custom-user)
- [대규모 더미 데이터 생성 시 N+1 문제 해결](https://velog.io/@sleekydevzero86/jpa-n-plus-1-solution)

