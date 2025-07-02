![image](https://github.com/user-attachments/assets/0ab4c671-8dfc-4498-8f6d-47b3b9d5ab4d)
![image](https://github.com/user-attachments/assets/68edacdd-3ccb-4f2c-8371-a2cca89aca93)

<br/>

## 프로젝트 개요

공정거래위원회에서 제공하는 통신판매사업자 CSV 정보를 기반으로, **파일을 읽고**, 공공 API를 활용해 추가 정보를 수집 후 **DB에 저장**하는 백엔드 서비스입니다.

---

## 아키텍처 개요

### 전체 데이터 흐름

```
[CSV 파일]
    │
    ▼
[CsvService]  --(사업자 리스트 추출)--> [CoSellerService]
    │                                         │
    │-------------------비동기-----------------│
    ▼                                         ▼
[CorpApiService]                        [RegionApiService]
 (공공데이터 API)                        (주소 API)
    │                                         │
    └-------------------결과-------------------┘
                      │
                      ▼
                [CorpMast] (엔티티 변환)
                      │
                      ▼
      [CorpMastStore / JpaCorpMastStore]
                      │
                      ▼
                   [H2 DB/PostgreSql]
                      │
                      ▼
         [CoSellerRestController]
                      │
                      ▼
           [클라이언트(JSP/프론트)]
─────────────────────────────────────────────
공통 지원: ApiResponse, 예외처리, 로깅, 설정
```

---

## 계층 구조 및 주요 역할

- **Controller**: API 엔드포인트 제공 (`CoSellerRestController`)
- **Service**: 비즈니스 로직, 외부 API 연동, 병렬 처리 (`CoSellerService`, `CorpApiService`, `RegionApiService`, `CsvService`)
- **Repository/Store**: DB 접근, 저장소 추상화 (`CorpMastRepository`, `CorpMastStore`, `JpaCorpMastStore`)
- **Domain/Entity**: 핵심 데이터 모델 (`CorpMast`)
- **DTO/Properties**: 데이터 전달 객체, 외부 API 설정
- **공통**: 예외 처리, 응답 포맷, 유효성 검증, 로깅, 설정 등

---

## 기술 스택

| 구분       | 내용                           |
| ---------- | ------------------------------ |
| 언어       | Java 17                        |
| 프레임워크 | Spring Boot 3.x                |
| DB         | H2 / PostgreSql /Redis         |
| 빌드       | Gradle                         |
| 로깅       | Logback (`logback-spring.xml`) |
| 병렬 처리  | `@Async` + `CompletableFuture` |
| API 통신   | `RestTemplate`                 |
| 테스트     | JUnit5, AssertJ                |

---

## 기능 요구사항

- 다섯개의 기능 상세 구현 완료

## 비즈니스 고려사항

### API 출처 변경시 확장성 고려

- yml 파일에서 API 설정을 읽어오도록 구현
- API 변경시 yml 파일에서 `url`,`endpoint`,`apiKey`,`queryParams` 변경하여 적용 가능

### 저장소 변경시 확장성 고려

- 추상화 클래스 `CorpMastStore`를 생성하여 JpaRepository를 래핑한 `JpaCorpMastStore`로 JpaRepository 를 사용하도록 구현

### 병렬 처리

- '@Async'를 활용한 비동기 메서드를 통해 'CompletableFuture' 기반 병렬 API 호출 구현
- 실패한 항목은 로깅 후 skip 처리하여 유연성 확보

### 로깅 전략

- `logback-spring.xml` 설정
  - `INFO` 이상 로그는 콘솔 및 파일에 출력
  - 로그 파일은 날짜별로 분리 & 7일 보관

### 예외 처리

- `CustomException` 기반 공통 예외 클래스 정의
- 상황별 커스텀 예외:
  - `CsvParsingException`: CSV 파싱 실패
  - `ExternalApiException`: 공공 API 호출 실패
- 모든 예외는 `GlobalExceptionHandler`에서 처리
- Client 요청값은 'Enum'으로 받아 '@ValidEnum' 어노테이션을 추가하여 검증 처리
- 일관된 응답 포맷 반환: `ApiResponse.class` 사용

### 테스트 코드

- 각 레이어 별 기능 단위 테스트 작성
- 정상 케이스 및 예외 케이스 테스트 포함하여 안정성 검증

---

## 추가 참고 사항

- CSV 를 다운 API가 없어 임의로 생성한 csv파일로 작업 진행(서울특별시\_강남구.csv)
- 공통 응답 포맷(`resultCode`,`resultMsg`,`data`)생성
- 데이터 저장 로직은 전체 롤백이 아닌, 일부 실패 허용 구조로 설계되어 트랜잭션은 미사용 및 실패한 건에 대해서는 로깅 후 skip 처리( saveAll실패시 개별 저장됨)

---
