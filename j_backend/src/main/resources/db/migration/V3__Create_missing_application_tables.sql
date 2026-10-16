-- =====================================================================================
-- Flyway 마이그레이션: 애플리케이션 테이블 생성
-- 프로젝트: Public Data Harvester J-Backend
-- 마이그레이션 버전: V3
-- 작성일: 2026-10-17
-- 설명: 엔티티가 기대하는 테이블이 없으면 만든다
-- =====================================================================================

-- =====================================================================================
-- 1. 회원 테이블 (members)
-- 설명: 시스템 사용자 정보를 관리하는 테이블
-- =====================================================================================
CREATE TABLE IF NOT EXISTS members (
    id BIGSERIAL PRIMARY KEY,                                      -- 회원 고유 ID (자동증가)
    username VARCHAR(50) NOT NULL UNIQUE,                          -- 사용자명 (고유)
    password VARCHAR(100) NOT NULL,                                -- 암호화된 비밀번호
    nickname VARCHAR(50) NOT NULL,                                 -- 닉네임
    email VARCHAR(100) UNIQUE,                                     -- 이메일 (고유, 선택)
    api_key VARCHAR(64) UNIQUE,                                    -- API 키 (고유)
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',                 -- 회원 상태
    role VARCHAR(20) NOT NULL DEFAULT 'USER',                      -- 사용자 권한
    last_login_at TIMESTAMP,                                       -- 마지막 로그인 시간
    login_fail_count INTEGER NOT NULL DEFAULT 0,                   -- 로그인 실패 횟수
    account_locked_at TIMESTAMP,                                   -- 계정 잠금 시간
    approved_by BIGINT,                                            -- 승인한 관리자 ID
    approved_at TIMESTAMP,                                         -- 승인 시간
    password_changed_at TIMESTAMP,                                 -- 비밀번호 마지막 변경 시간
    password_change_count INTEGER NOT NULL DEFAULT 0,              -- 비밀번호 변경 횟수
    last_password_change_date DATE,                                -- 마지막 비밀번호 변경 날짜
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP       -- 수정일시
);

-- 회원 테이블 코멘트
COMMENT ON TABLE members IS '시스템 사용자 정보 관리 테이블';
COMMENT ON COLUMN members.id IS '회원 고유 ID (자동증가)';
COMMENT ON COLUMN members.username IS '사용자명 (로그인용, 고유값)';
COMMENT ON COLUMN members.password IS '암호화된 비밀번호';
COMMENT ON COLUMN members.nickname IS '사용자 닉네임';
COMMENT ON COLUMN members.email IS '이메일 주소 (고유값, 선택사항)';
COMMENT ON COLUMN members.api_key IS 'API 인증용 키 (고유값)';
COMMENT ON COLUMN members.status IS '회원 상태 (PENDING:승인대기, APPROVED:승인됨, REJECTED:거부됨, SUSPENDED:정지됨, WITHDRAWN:탈퇴함)';
COMMENT ON COLUMN members.role IS '사용자 권한 (USER:일반사용자, MANAGER:관리자, ADMIN:최고관리자)';
COMMENT ON COLUMN members.last_login_at IS '마지막 로그인 시간';
COMMENT ON COLUMN members.login_fail_count IS '연속 로그인 실패 횟수';
COMMENT ON COLUMN members.account_locked_at IS '계정 잠금 시간';
COMMENT ON COLUMN members.approved_by IS '회원 승인을 처리한 관리자 ID';
COMMENT ON COLUMN members.approved_at IS '회원 승인 처리 시간';
COMMENT ON COLUMN members.password_changed_at IS '비밀번호 마지막 변경 시간';
COMMENT ON COLUMN members.password_change_count IS '비밀번호 변경 횟수';
COMMENT ON COLUMN members.last_password_change_date IS '마지막 비밀번호 변경 날짜';
COMMENT ON COLUMN members.create_date IS '계정 생성일시';
COMMENT ON COLUMN members.modify_date IS '정보 수정일시';

-- =====================================================================================
-- 2. 회원 비밀번호 이력 테이블 (member_password_history)
-- 설명: 회원의 비밀번호 변경 이력을 관리하여 최근 비밀번호 재사용 방지
-- =====================================================================================
CREATE TABLE IF NOT EXISTS member_password_history (
    id BIGSERIAL PRIMARY KEY,                                      -- 이력 고유 ID
    member_id BIGINT NOT NULL,                                     -- 회원 ID (외래키)
    password_hash VARCHAR(255) NOT NULL,                           -- 암호화된 비밀번호
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,       -- 생성일시
    action VARCHAR(255),                                           -- 수행 작업
    result VARCHAR(255),                                           -- 처리 결과
    message VARCHAR(255),                                          -- 메시지
    ip_address VARCHAR(255),                                       -- 요청 IP
    user_agent VARCHAR(255),                                       -- User-Agent
    timestamp TIMESTAMP,                                           -- 기록 시각

    CONSTRAINT fk_member_password_history_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

-- 회원 비밀번호 이력 테이블 코멘트
COMMENT ON TABLE member_password_history IS '회원 비밀번호 변경 이력 관리 테이블';
COMMENT ON COLUMN member_password_history.id IS '이력 고유 ID';
COMMENT ON COLUMN member_password_history.member_id IS '회원 ID (members 테이블 참조)';
COMMENT ON COLUMN member_password_history.password_hash IS '암호화된 비밀번호 해시값';
COMMENT ON COLUMN member_password_history.created_at IS '비밀번호 생성(변경) 일시';
COMMENT ON COLUMN member_password_history.action IS '수행된 작업';
COMMENT ON COLUMN member_password_history.result IS '처리 결과';
COMMENT ON COLUMN member_password_history.message IS '처리 메시지';
COMMENT ON COLUMN member_password_history.ip_address IS '요청 IP';
COMMENT ON COLUMN member_password_history.user_agent IS '요청 User-Agent';
COMMENT ON COLUMN member_password_history.timestamp IS '기록 시각';

-- =====================================================================================
-- 3. 비밀번호 재설정 토큰 테이블 (password_reset_token)
-- 설명: 비밀번호 찾기 토큰의 만료와 사용 여부를 관리한다
-- =====================================================================================
CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGSERIAL PRIMARY KEY,                                      -- 토큰 고유 ID
    token VARCHAR(255) NOT NULL UNIQUE,                            -- 재설정 토큰 (고유)
    expires_at TIMESTAMP NOT NULL,                                 -- 만료 시각
    used BOOLEAN NOT NULL DEFAULT FALSE,                           -- 사용 여부
    member_id BIGINT NOT NULL,                                     -- 회원 ID (외래키)
    client_ip VARCHAR(45),                                         -- 요청 IP
    user_agent VARCHAR(500),                                       -- User-Agent
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 수정일시

    CONSTRAINT fk_password_reset_token_member FOREIGN KEY (member_id) REFERENCES members(id)
);

-- 비밀번호 재설정 토큰 테이블 코멘트
COMMENT ON TABLE password_reset_token IS '비밀번호 재설정 토큰 관리 테이블';
COMMENT ON COLUMN password_reset_token.id IS '토큰 고유 ID';
COMMENT ON COLUMN password_reset_token.token IS '재설정 토큰 (고유값)';
COMMENT ON COLUMN password_reset_token.expires_at IS '토큰 만료 시각';
COMMENT ON COLUMN password_reset_token.used IS '토큰 사용 여부';
COMMENT ON COLUMN password_reset_token.member_id IS '회원 ID (members 테이블 참조)';
COMMENT ON COLUMN password_reset_token.client_ip IS '요청 클라이언트 IP';
COMMENT ON COLUMN password_reset_token.user_agent IS '요청 User-Agent';
COMMENT ON COLUMN password_reset_token.create_date IS '레코드 생성일시';
COMMENT ON COLUMN password_reset_token.modify_date IS '레코드 수정일시';

-- =====================================================================================
-- 4. 법인 마스터 테이블 (corp_mast)
-- 설명: 법인 기본 정보를 관리하는 마스터 테이블
-- =====================================================================================
CREATE TABLE IF NOT EXISTS corp_mast (
    id BIGSERIAL PRIMARY KEY,                                      -- 법인 고유 ID
    seller_id VARCHAR(100) NOT NULL,                               -- 판매자 ID
    biz_nm VARCHAR(200) NOT NULL,                                  -- 사업자명
    biz_no VARCHAR(20) NOT NULL,                                   -- 사업자번호 (고유)
    corp_reg_no VARCHAR(20) NOT NULL,                              -- 법인등록번호
    region_cd VARCHAR(20) NOT NULL,                                -- 지역코드
    si_nm VARCHAR(50) NOT NULL,                                    -- 시/도명
    sgg_nm VARCHAR(50) NOT NULL,                                   -- 시군구명
    username VARCHAR(100) NOT NULL,                                -- 등록자 사용자명
    description VARCHAR(2000),                                     -- 상세 설명
    rep_nm VARCHAR(100),                                           -- 대표자명
    estb_dt VARCHAR(20),                                           -- 설립일자
    road_nm_addr VARCHAR(200),                                     -- 도로명주소
    jibun_addr VARCHAR(200),                                       -- 지번주소
    corp_status VARCHAR(50),                                       -- 법인상태
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 수정일시

    CONSTRAINT corp_mast_unique UNIQUE (biz_no)
);

-- 법인 마스터 테이블 코멘트
COMMENT ON TABLE corp_mast IS '법인 기본 정보 관리 마스터 테이블';
COMMENT ON COLUMN corp_mast.id IS '법인 고유 ID (자동증가)';
COMMENT ON COLUMN corp_mast.seller_id IS '판매자 ID (예: 2025-서울강남-01714)';
COMMENT ON COLUMN corp_mast.biz_nm IS '사업자명 (법인명)';
COMMENT ON COLUMN corp_mast.biz_no IS '사업자번호 (고유값)';
COMMENT ON COLUMN corp_mast.corp_reg_no IS '법인등록번호';
COMMENT ON COLUMN corp_mast.region_cd IS '행정구역 지역코드';
COMMENT ON COLUMN corp_mast.si_nm IS '시/도명 (예: 서울특별시)';
COMMENT ON COLUMN corp_mast.sgg_nm IS '시군구명 (예: 강남구)';
COMMENT ON COLUMN corp_mast.username IS '데이터 등록자 사용자명';
COMMENT ON COLUMN corp_mast.description IS '법인 상세 설명';
COMMENT ON COLUMN corp_mast.rep_nm IS '대표자명 (법인 대표자 이름)';
COMMENT ON COLUMN corp_mast.estb_dt IS '설립일자 (YYYYMMDD 형식)';
COMMENT ON COLUMN corp_mast.road_nm_addr IS '도로명주소 (새주소 체계)';
COMMENT ON COLUMN corp_mast.jibun_addr IS '지번주소 (구주소 체계)';
COMMENT ON COLUMN corp_mast.corp_status IS '법인상태 (예: 계속(수익), 휴업, 폐업)';
COMMENT ON COLUMN corp_mast.create_date IS '데이터 생성일시';
COMMENT ON COLUMN corp_mast.modify_date IS '데이터 수정일시';

-- =====================================================================================
-- 5. 법인 마스터 이력 테이블 (corp_mast_history)
-- 설명: 법인 정보 변경 및 처리 이력을 추적하는 테이블
-- =====================================================================================
CREATE TABLE IF NOT EXISTS corp_mast_history (
    id BIGSERIAL PRIMARY KEY,                                      -- 이력 고유 ID
    username VARCHAR(255),                                         -- 작업 수행자
    action VARCHAR(255),                                           -- 수행 액션
    biz_no VARCHAR(255),                                           -- 관련 사업자번호
    result VARCHAR(255),                                           -- 처리 결과
    message TEXT,                                                  -- 상세 메시지
    timestamp TIMESTAMP,                                           -- 처리 시간
    corp_mast_id BIGINT,                                           -- 법인 마스터 ID (외래키, NULL 허용)

    CONSTRAINT fk_corp_mast_history_corp_mast FOREIGN KEY (corp_mast_id) REFERENCES corp_mast(id) ON DELETE SET NULL
);

-- 법인 마스터 이력 테이블 코멘트
COMMENT ON TABLE corp_mast_history IS '법인 정보 변경 및 처리 이력 추적 테이블';
COMMENT ON COLUMN corp_mast_history.id IS '이력 고유 ID';
COMMENT ON COLUMN corp_mast_history.username IS '작업 수행자 사용자명';
COMMENT ON COLUMN corp_mast_history.action IS '수행된 액션 종류';
COMMENT ON COLUMN corp_mast_history.biz_no IS '관련 사업자번호';
COMMENT ON COLUMN corp_mast_history.result IS '처리 결과';
COMMENT ON COLUMN corp_mast_history.message IS '상세 처리 메시지 또는 오류 내용';
COMMENT ON COLUMN corp_mast_history.timestamp IS '작업 수행 시간';
COMMENT ON COLUMN corp_mast_history.corp_mast_id IS '관련 법인 마스터 ID (삭제시 NULL)';

-- =====================================================================================
-- 6. 파일 정보 테이블 (files)
-- 설명: 업로드된 파일의 메타데이터 및 정보를 관리하는 테이블
-- =====================================================================================
CREATE TABLE IF NOT EXISTS files (
    id BIGSERIAL PRIMARY KEY,                                      -- 파일 고유 ID
    original_file_name VARCHAR(255) NOT NULL,                      -- 원본 파일명
    stored_file_name VARCHAR(255) NOT NULL UNIQUE,                 -- 저장된 파일명 (고유)
    content_type VARCHAR(255) NOT NULL,                            -- 파일 MIME 타입
    file_size BIGINT NOT NULL,                                     -- 파일 크기 (바이트)
    description VARCHAR(1000),                                     -- 파일 설명
    upload_time TIMESTAMP NOT NULL,                                -- 업로드 시간
    last_modified_time TIMESTAMP NOT NULL,                         -- 마지막 수정 시간
    uploader_id BIGINT,                                            -- 업로더 회원 ID (외래키)
    uploader_name VARCHAR(50),                                     -- 업로더명
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 수정일시

    CONSTRAINT fk_files_uploader FOREIGN KEY (uploader_id) REFERENCES members(id) ON DELETE SET NULL
);

-- 파일 정보 테이블 코멘트
COMMENT ON TABLE files IS '업로드된 파일의 메타데이터 및 관리 정보 테이블';
COMMENT ON COLUMN files.id IS '파일 고유 ID';
COMMENT ON COLUMN files.original_file_name IS '사용자가 업로드한 원본 파일명';
COMMENT ON COLUMN files.stored_file_name IS '서버에 저장된 파일명 (고유값)';
COMMENT ON COLUMN files.content_type IS '파일 MIME 타입 (예: image/jpeg, text/csv)';
COMMENT ON COLUMN files.file_size IS '파일 크기 (바이트 단위)';
COMMENT ON COLUMN files.description IS '파일에 대한 설명 (최대 1000자)';
COMMENT ON COLUMN files.upload_time IS '파일 업로드 시간';
COMMENT ON COLUMN files.last_modified_time IS '파일 정보 마지막 수정 시간';
COMMENT ON COLUMN files.uploader_id IS '파일을 업로드한 회원 ID';
COMMENT ON COLUMN files.uploader_name IS '업로더 이름';
COMMENT ON COLUMN files.create_date IS '레코드 생성일시';
COMMENT ON COLUMN files.modify_date IS '레코드 수정일시';

-- =====================================================================================
-- 7. CSV 배치 처리 이력 테이블 (csv_batch_history)
-- 설명: CSV 파일의 배치 처리 이력을 관리하는 테이블
-- =====================================================================================
CREATE TABLE IF NOT EXISTS csv_batch_history (
    id BIGSERIAL PRIMARY KEY,                                      -- 배치 이력 고유 ID
    city VARCHAR(255),                                             -- 처리 대상 시/도
    district VARCHAR(255),                                         -- 처리 대상 시군구
    file_name VARCHAR(255),                                        -- 처리된 CSV 파일명
    record_count INTEGER NOT NULL DEFAULT 0,                       -- 처리된 레코드 수
    status VARCHAR(255),                                           -- 처리 상태
    message VARCHAR(255),                                          -- 처리 메시지
    timestamp TIMESTAMP                                            -- 처리 시간
);

-- CSV 배치 처리 이력 테이블 코멘트
COMMENT ON TABLE csv_batch_history IS 'CSV 파일 배치 처리 이력 관리 테이블';
COMMENT ON COLUMN csv_batch_history.id IS '배치 처리 이력 고유 ID';
COMMENT ON COLUMN csv_batch_history.city IS '처리 대상 시/도명';
COMMENT ON COLUMN csv_batch_history.district IS '처리 대상 시군구명';
COMMENT ON COLUMN csv_batch_history.file_name IS '처리된 CSV 파일명';
COMMENT ON COLUMN csv_batch_history.record_count IS '처리된 레코드 수';
COMMENT ON COLUMN csv_batch_history.status IS '처리 상태';
COMMENT ON COLUMN csv_batch_history.message IS '처리 결과 메시지 또는 오류 내용';
COMMENT ON COLUMN csv_batch_history.timestamp IS '배치 처리 시간';

-- =====================================================================================
-- 8. 헬스 체크 테이블 (health_checks)
-- 설명: 컴포넌트별 헬스 체크 결과를 저장한다
-- =====================================================================================
CREATE TABLE IF NOT EXISTS health_checks (
    id BIGSERIAL PRIMARY KEY,                                      -- 헬스 체크 고유 ID
    component VARCHAR(50) NOT NULL,                                -- 컴포넌트
    status VARCHAR(20) NOT NULL,                                   -- 상태
    message VARCHAR(1000),                                         -- 메시지
    response_time BIGINT,                                          -- 응답 시간 (밀리초)
    check_type VARCHAR(100),                                       -- 체크 종류
    details VARCHAR(2000),                                         -- 상세
    checked_at TIMESTAMP,                                          -- 체크 시각
    expires_at TIMESTAMP,                                          -- 만료 시각
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP       -- 수정일시
);

-- 헬스 체크 테이블 코멘트
COMMENT ON TABLE health_checks IS '컴포넌트별 헬스 체크 결과 테이블';
COMMENT ON COLUMN health_checks.id IS '헬스 체크 고유 ID';
COMMENT ON COLUMN health_checks.component IS '검사 대상 컴포넌트';
COMMENT ON COLUMN health_checks.status IS '검사 상태';
COMMENT ON COLUMN health_checks.message IS '검사 메시지';
COMMENT ON COLUMN health_checks.response_time IS '응답 시간 (밀리초)';
COMMENT ON COLUMN health_checks.check_type IS '체크 종류';
COMMENT ON COLUMN health_checks.details IS '검사 상세';
COMMENT ON COLUMN health_checks.checked_at IS '체크 시각';
COMMENT ON COLUMN health_checks.expires_at IS '결과 만료 시각';
COMMENT ON COLUMN health_checks.create_date IS '레코드 생성일시';
COMMENT ON COLUMN health_checks.modify_date IS '레코드 수정일시';

-- =====================================================================================
-- 9. 시스템 헬스 테이블 (system_health)
-- 설명: 전체 컴포넌트 헬스 요약을 저장한다
-- =====================================================================================
CREATE TABLE IF NOT EXISTS system_health (
    id BIGSERIAL PRIMARY KEY,                                      -- 시스템 헬스 고유 ID
    overall_status VARCHAR(20) NOT NULL,                           -- 전체 상태
    total_components INTEGER,                                      -- 전체 컴포넌트 수
    healthy_components INTEGER,                                    -- 정상 수
    unhealthy_components INTEGER,                                  -- 장애 수
    unknown_components INTEGER,                                    -- 미확인 수
    details VARCHAR(2000),                                         -- 상세
    checked_at TIMESTAMP,                                          -- 체크 시각
    expires_at TIMESTAMP,                                          -- 만료 시각
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP       -- 수정일시
);

-- 시스템 헬스 테이블 코멘트
COMMENT ON TABLE system_health IS '전체 시스템 헬스 요약 테이블';
COMMENT ON COLUMN system_health.id IS '시스템 헬스 고유 ID';
COMMENT ON COLUMN system_health.overall_status IS '전체 상태';
COMMENT ON COLUMN system_health.total_components IS '전체 컴포넌트 수';
COMMENT ON COLUMN system_health.healthy_components IS '정상 컴포넌트 수';
COMMENT ON COLUMN system_health.unhealthy_components IS '장애 컴포넌트 수';
COMMENT ON COLUMN system_health.unknown_components IS '미확인 컴포넌트 수';
COMMENT ON COLUMN system_health.details IS '요약 상세';
COMMENT ON COLUMN system_health.checked_at IS '체크 시각';
COMMENT ON COLUMN system_health.expires_at IS '결과 만료 시각';
COMMENT ON COLUMN system_health.create_date IS '레코드 생성일시';
COMMENT ON COLUMN system_health.modify_date IS '레코드 수정일시';

-- =====================================================================================
-- 성능 최적화를 위한 인덱스 생성
-- =====================================================================================

-- 법인 마스터 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_corp_mast_biz_no ON corp_mast(biz_no);                    -- 사업자번호 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_corp_reg_no ON corp_mast(corp_reg_no);          -- 법인등록번호 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_seller_id ON corp_mast(seller_id);              -- 판매자ID 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_username ON corp_mast(username);                -- 등록자별 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_location ON corp_mast(si_nm, sgg_nm);           -- 지역별 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_region_cd ON corp_mast(region_cd);              -- 지역코드 조회용

-- 비밀번호 재설정 토큰 인덱스
CREATE INDEX IF NOT EXISTS idx_password_reset_token_token ON password_reset_token(token);           -- 토큰 조회용
CREATE INDEX IF NOT EXISTS idx_password_reset_token_member_id ON password_reset_token(member_id);   -- 회원별 토큰 조회용
CREATE INDEX IF NOT EXISTS idx_password_reset_token_expires_at ON password_reset_token(expires_at); -- 만료 시각 조회용

-- 헬스 체크 인덱스
CREATE INDEX IF NOT EXISTS idx_health_checks_component ON health_checks(component);      -- 컴포넌트별 조회용
CREATE INDEX IF NOT EXISTS idx_health_checks_status ON health_checks(status);            -- 상태별 조회용
CREATE INDEX IF NOT EXISTS idx_health_checks_create_date ON health_checks(create_date);  -- 생성일 정렬용

-- 시스템 헬스 인덱스
CREATE INDEX IF NOT EXISTS idx_system_health_create_date ON system_health(create_date);              -- 생성일 정렬용
CREATE INDEX IF NOT EXISTS idx_system_health_overall_status ON system_health(overall_status);        -- 전체 상태 조회용
