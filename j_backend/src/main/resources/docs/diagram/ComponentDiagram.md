# Component Diagram - Antock Public Data Harvester

```plantuml
@startuml
package "Antock Public Data Harvester" {

  ' 인터페이스 정의
  interface MemberAuth
  interface CorpManagement
  interface FileManagement
  interface Dashboard
  interface AdminControl
  interface CsvBatch
  interface RegionData
  interface CacheManagement
  interface SecurityControl
  interface ExternalAPI

  ' 핵심 컴포넌트
  component MemberService {
    MemberAuth -- [MemberService]
    [MemberService] ..> CacheManagement
    [MemberService] ..> SecurityControl
  }

  component CorpMastService {
    CorpManagement -- [CorpMastService]
    [CorpMastService] ..> CacheManagement
    [CorpMastService] ..> ExternalAPI
  }

  component FileService {
    FileManagement -- [FileService]
    [FileService] ..> StorageAdapter
  }

  component DashboardService {
    Dashboard -- [DashboardService]
    [DashboardService] ..> CorpManagement
    [DashboardService] ..> RegionData
  }

  component AdminService {
    AdminControl -- [AdminService]
    [AdminService] ..> MemberAuth
    [AdminService] ..> CacheManagement
    [AdminService] ..> SecurityControl
  }

  component CsvBatchService {
    CsvBatch -- [CsvBatchService]
    [CsvBatchService] ..> CorpManagement
    [CsvBatchService] ..> ExternalAPI
  }

  component RegionService {
    RegionData -- [RegionService]
    [RegionService] ..> ExternalAPI
  }

  ' 캐시 관리 컴포넌트
  component CacheAdapter {
    CacheManagement -- [CacheAdapter]
    [CacheAdapter] --> RedisAdapter
    [CacheAdapter] --> MemoryAdapter
  }

  component RedisAdapter {
    note right of [RedisAdapter]
      Redis 기반 캐시
      - 멤버 캐시
      - Rate Limiting
      - 세션 관리
    end note
  }

  component MemoryAdapter {
    note right of [MemoryAdapter]
      메모리 기반 Fallback
      - 캐시 실패시 대체
      - 로컬 Rate Limiting
    end note
  }

  ' 보안 관리 컴포넌트
  component SecurityAdapter {
    SecurityControl -- [SecurityAdapter]
    [SecurityAdapter] --> JwtTokenProvider
    [SecurityAdapter] --> RateLimitAdapter
  }

  component JwtTokenProvider {
    note right of [JwtTokenProvider]
      JWT 토큰 관리
      - 토큰 생성/검증
      - 사용자 인증
    end note
  }

  component RateLimitAdapter {
    note right of [RateLimitAdapter]
      Rate Limiting
      - Redis/Memory 기반
      - 자동 차단
      - 화이트리스트
    end note
  }

  ' 스토리지 어댑터
  component StorageAdapter {
    [StorageAdapter] --> LocalStorage
    [StorageAdapter] --> MinioStorage
  }

  component LocalStorage {
    note right of [LocalStorage]
      로컬 파일 스토리지
    end note
  }

  component MinioStorage {
    note right of [MinioStorage]
      MinIO 오브젝트 스토리지
    end note
  }

  ' 외부 API 어댑터
  component ExternalAPIAdapter {
    ExternalAPI -- [ExternalAPIAdapter]
    [ExternalAPIAdapter] --> CorpAPIAdapter
    [ExternalAPIAdapter] --> RegionAPIAdapter
  }

  component CorpAPIAdapter {
    note right of [CorpAPIAdapter]
      법인 정보 API 연동
      - 사업자등록번호 조회
      - 법인등록번호 검증
    end note
  }

  component RegionAPIAdapter {
    note right of [RegionAPIAdapter]
      지역 정보 API 연동
      - 주소 검색
      - 지역코드 변환
    end note
  }

  ' 웹 컨트롤러 레이어
  package "Web Controllers" {
    component MemberWebController
    component CorpWebController
    component FileWebController
    component AdminWebController
    component DashboardWebController

    MemberWebController --> MemberService
    CorpWebController --> CorpMastService
    FileWebController --> FileService
    AdminWebController --> AdminService
    DashboardWebController --> DashboardService
  }

  ' API 컨트롤러 레이어
  package "API Controllers" {
    component MemberAPIController
    component CorpAPIController
    component FileAPIController
    component AdminAPIController
    component DashboardAPIController

    MemberAPIController --> MemberService
    CorpAPIController --> CorpMastService
    FileAPIController --> FileService
    AdminAPIController --> AdminService
    DashboardAPIController --> DashboardService
  }

  ' 데이터베이스 레이어
  package "Data Persistence" {
    database MySQL {
      component MemberRepository
      component CorpMastRepository
      component FileRepository
      component CsvBatchRepository
    }

    MemberService --> MemberRepository
    CorpMastService --> CorpMastRepository
    FileService --> FileRepository
    CsvBatchService --> CsvBatchRepository
  }

  ' 스케줄러
  component SchedulerService {
    [SchedulerService] --> CsvBatchService
    [SchedulerService] --> MemberService

    note right of [SchedulerService]
      배치 작업 스케줄링
      - CSV 자동 처리
      - 패스워드 만료 알림
      - 계정 잠금 해제
    end note
  }

  ' 글로벌 설정
  component GlobalConfig {
    note right of [GlobalConfig]
      전역 설정
      - 시큐리티 설정
      - 스웨거 설정
      - Redis/MinIO 설정
    end note
  }

  GlobalConfig --> SecurityAdapter
  GlobalConfig --> CacheAdapter
  GlobalConfig --> StorageAdapter
}
@enduml
```

## 컴포넌트 설명

### 1. 핵심 서비스 컴포넌트

#### MemberService

- **역할**: 회원 관리 및 인증
- **주요 기능**:
  - 회원 가입/로그인
  - 패스워드 관리
  - 권한 관리 (ADMIN, MANAGER, USER)
  - 계정 상태 관리 (승인 대기, 승인, 정지, 거부, 탈퇴)

#### CorpMastService

- **역할**: 법인 정보 관리
- **주요 기능**:
  - 법인 정보 CRUD
  - 사업자번호/법인등록번호 검증
  - 지역별 법인 통계
  - Excel 내보내기

#### FileService

- **역할**: 파일 업로드/다운로드 관리
- **주요 기능**:
  - 파일 업로드/다운로드
  - 파일 메타데이터 관리
  - 스토리지 전략 (Local/MinIO)

#### DashboardService

- **역할**: 대시보드 및 통계
- **주요 기능**:
  - 지역별 통계
  - 최근 활동 내역
  - 시스템 현황

#### AdminService

- **역할**: 시스템 관리
- **주요 기능**:
  - 시스템 메트릭 모니터링
  - 캐시 관리
  - 회원 데이터 관리

#### CsvBatchService

- **역할**: CSV 배치 처리
- **주요 기능**:
  - 스케줄링된 CSV 처리
  - 외부 API 연동
  - 배치 이력 관리

### 2. 어댑터 컴포넌트

#### CacheAdapter

- **Redis 기반**: 분산 캐시, Rate Limiting
- **Memory 기반**: Redis 장애시 Fallback

#### SecurityAdapter

- **JWT Provider**: 토큰 기반 인증
- **Rate Limiter**: 요청 제한 및 보안

#### StorageAdapter

- **Local Storage**: 로컬 파일 시스템
- **MinIO Storage**: 오브젝트 스토리지

#### ExternalAPIAdapter

- **Corp API**: 법인 정보 조회
- **Region API**: 지역 정보 조회

### 3. 아키텍처 특징

1. **레이어드 아키텍처**: Controller → Service → Repository
2. **어댑터 패턴**: 외부 시스템 연동
3. **Fallback 메커니즘**: Redis 장애시 메모리 캐시 사용
4. **전략 패턴**: 파일 스토리지 전략 선택
5. **스케줄링**: 배치 작업 자동화
6. **캐싱**: 성능 최적화
7. **Rate Limiting**: 보안 및 안정성

이 컴포넌트 다이어그램은 시스템의 주요 구성 요소와 그들 간의 의존성을 명확히 보여주며, 확장 가능하고 유지보수가 용이한 아키텍처를 나타냅니다.
