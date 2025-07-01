![image](https://github.com/user-attachments/assets/0ab4c671-8dfc-4498-8f6d-47b3b9d5ab4d)
![image](https://github.com/user-attachments/assets/8d890b43-01b5-4eaf-83f6-6a5fe46d2ec8)


<br/>


---

## 화면 구성 (*) 추후변경가능..

- **대시보드**: 주요 지표, 최근 활동, 시스템 상태
- **데이터 수집**: 수집 현황, 수집 실행/이력
- **데이터 관리**: 데이터 목록, 품질 검증, 내보내기
- **스케줄 관리**: 스케줄 등록/수정/삭제, 크론 관리
- **모니터링**: 통계, 트렌드, 성공률
- **설정**: 환경설정, 알림 설정, (사용자 관리)

---

## 기술 스택

- Java, Spring Boot
- (DB) redis, PostgreSQL 등
- (프론트) Jsp
- (기타) Docker, JPA, Swagger, JUnit 등

---

## 설치 및 실행

```bash
# 의존성 설치
./gradlew build

# 애플리케이션 실행
java -jar build/libs/enterprise-info-crawler.jar
```

---

## 기여 및 문의

- Pull Request, Issue 등록 환영
- 문의: [your-email@example.com]



# 통신판매사업자 정보 관리 시스템

## 프로젝트 개요

본 시스템은 통신판매사업자(기업) 정보를 자동으로 수집, 관리, 분석하는 통합 플랫폼입니다.  
크롤링, 데이터 품질 검증, 스케줄링, 모니터링, 알림 등 실무에 필요한 다양한 기능을 제공합니다.

---

## 주요 기능

### 1. 대시보드
- 전체 등록 업체, 오늘 수집, 수집 성공률, API 호출 수 등 주요 지표 시각화
- 최근 활동 및 시스템 상태(연결, 서비스, 스케줄러) 실시간 표시

### 2. 데이터 수집
- 다양한 기업 정보 자동 크롤링
- 정기적/수동 데이터 수집 지원

### 3. 데이터 관리
- 수집 데이터 목록/상세 조회
- 데이터 품질 검증(중복, 누락 체크)
- 데이터 필터링 및 정렬

### 4. 데이터 모니터링 & 분석
- 수집 데이터 통계 및 트렌드 분석
- 수집 성공률 모니터링

### 5. 스케줄 관리
- 크론 표현식 기반 정기적 데이터 수집 자동화
- 스케줄 등록/수정/삭제

### 6. 데이터 내보내기
- Excel, JSON 등 다양한 포맷 지원
- 조건별 데이터 다운로드

### 7. 알림 시스템
- 수집 완료/실패, 데이터 품질 이슈 등 실시간 알림(이메일, 슬랙 등)

### 8. 시스템 관리
- 환경설정(application.yml)
- 로그 관리(logback-spring.xml)
- (선택) 사용자/권한 관리

---

## 폴더 구조

```
src/
└─ main/
├─ java/com/antock/enterprise_info_crawler/
│ ├─ api/ # API 엔드포인트
│ ├─ common/ # 공통 모듈
│ ├─ config/ # 환경설정
│ ├─ utils/ # 유틸리티
│ └─ ... # 기타 비즈니스 로직
└─ resources/
├─ application.yml # 환경설정 파일
├─ logback-spring.xml # 로그 설정
└─ csvFiles/ # 데이터 파일 저장
```
