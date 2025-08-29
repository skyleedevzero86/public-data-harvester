-- =====================================================================================
-- PostgreSQL 데이터베이스 생성 스크립트
-- 프로젝트: Public Data Harvester J-Backend
-- 작성일: 2024-12-19
-- 설명: 법인 정보 수집 및 관리를 위한 데이터베이스 스키마
-- =====================================================================================

-- 데이터베이스 설정
SET timezone = 'Asia/Seoul';
SET client_encoding = 'UTF8';

-- =====================================================================================
-- 기본 테이블 생성
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
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',                 -- 회원 상태 (PENDING, APPROVED, REJECTED, SUSPENDED, WITHDRAWN)
    role VARCHAR(20) NOT NULL DEFAULT 'USER',                      -- 사용자 권한 (USER, MANAGER, ADMIN)
    last_login_at TIMESTAMP,                                       -- 마지막 로그인 시간
    login_fail_count INTEGER NOT NULL DEFAULT 0,                   -- 로그인 실패 횟수
    account_locked_at TIMESTAMP,                                   -- 계정 잠금 시간
    approved_by BIGINT,                                            -- 승인한 관리자 ID
    approved_at TIMESTAMP,                                         -- 승인 시간
    password_changed_at TIMESTAMP,                                 -- 비밀번호 마지막 변경 시간
    password_change_count INTEGER NOT NULL DEFAULT 0,              -- 비밀번호 일일 변경 횟수
    last_password_change_date DATE,                                -- 마지막 비밀번호 변경 날짜
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 수정일시
    
    -- 제약조건
    CONSTRAINT members_status_check CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED', 'WITHDRAWN')),
    CONSTRAINT members_role_check CHECK (role IN ('USER', 'MANAGER', 'ADMIN')),
    CONSTRAINT members_login_fail_count_check CHECK (login_fail_count >= 0),
    CONSTRAINT members_password_change_count_check CHECK (password_change_count >= 0)
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
COMMENT ON COLUMN members.login_fail_count IS '연속 로그인 실패 횟수 (5회 초과시 계정 잠금)';
COMMENT ON COLUMN members.account_locked_at IS '계정 잠금 시간';
COMMENT ON COLUMN members.approved_by IS '회원 승인을 처리한 관리자 ID';
COMMENT ON COLUMN members.approved_at IS '회원 승인 처리 시간';
COMMENT ON COLUMN members.password_changed_at IS '비밀번호 마지막 변경 시간';
COMMENT ON COLUMN members.password_change_count IS '일일 비밀번호 변경 횟수 (최대 3회)';
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
    password_hash VARCHAR(100) NOT NULL,                           -- 암호화된 비밀번호
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,       -- 생성일시
    
    -- 외래키 제약조건
    CONSTRAINT fk_member_password_history_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

-- 회원 비밀번호 이력 테이블 코멘트
COMMENT ON TABLE member_password_history IS '회원 비밀번호 변경 이력 관리 테이블 (최근 비밀번호 재사용 방지)';
COMMENT ON COLUMN member_password_history.id IS '이력 고유 ID';
COMMENT ON COLUMN member_password_history.member_id IS '회원 ID (members 테이블 참조)';
COMMENT ON COLUMN member_password_history.password_hash IS '암호화된 비밀번호 해시값';
COMMENT ON COLUMN member_password_history.created_at IS '비밀번호 생성(변경) 일시';

-- =====================================================================================
-- 3. 법인 마스터 테이블 (corp_mast)
-- 설명: 법인 기본 정보를 관리하는 마스터 테이블
-- =====================================================================================
CREATE TABLE IF NOT EXISTS corp_mast (
    id BIGSERIAL PRIMARY KEY,                                      -- 법인 고유 ID
    seller_id VARCHAR(100) NOT NULL,                               -- 판매자 ID
    biz_nm VARCHAR(200) NOT NULL,                                  -- 사업자명
    biz_no VARCHAR(20) NOT NULL UNIQUE,                            -- 사업자번호 (고유)
    corp_reg_no VARCHAR(20) NOT NULL,                              -- 법인등록번호
    region_cd VARCHAR(20) NOT NULL,                                -- 지역코드
    si_nm VARCHAR(50) NOT NULL,                                    -- 시/도명
    sgg_nm VARCHAR(50) NOT NULL,                                   -- 시군구명
    username VARCHAR(100) NOT NULL,                                -- 등록자 사용자명
    description VARCHAR(2000),                                     -- 상세 설명
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 수정일시
    
    -- 제약조건
    CONSTRAINT corp_mast_biz_no_format CHECK (biz_no ~ '^[0-9]{3}-?[0-9]{2}-?[0-9]{5}$' OR LENGTH(REPLACE(biz_no, '-', '')) = 10),
    CONSTRAINT corp_mast_corp_reg_no_format CHECK (LENGTH(corp_reg_no) >= 10)
);

-- 법인 마스터 테이블 코멘트
COMMENT ON TABLE corp_mast IS '법인 기본 정보 관리 마스터 테이블';
COMMENT ON COLUMN corp_mast.id IS '법인 고유 ID (자동증가)';
COMMENT ON COLUMN corp_mast.seller_id IS '판매자 ID (예: 2025-서울강남-01714)';
COMMENT ON COLUMN corp_mast.biz_nm IS '사업자명 (법인명)';
COMMENT ON COLUMN corp_mast.biz_no IS '사업자번호 (10자리, 하이픈 포함 가능, 고유값)';
COMMENT ON COLUMN corp_mast.corp_reg_no IS '법인등록번호';
COMMENT ON COLUMN corp_mast.region_cd IS '행정구역 지역코드';
COMMENT ON COLUMN corp_mast.si_nm IS '시/도명 (예: 서울특별시)';
COMMENT ON COLUMN corp_mast.sgg_nm IS '시군구명 (예: 강남구)';
COMMENT ON COLUMN corp_mast.username IS '데이터 등록자 사용자명';
COMMENT ON COLUMN corp_mast.description IS '법인 상세 설명';
COMMENT ON COLUMN corp_mast.create_date IS '데이터 생성일시';
COMMENT ON COLUMN corp_mast.modify_date IS '데이터 수정일시';

-- =====================================================================================
-- 4. 법인 마스터 이력 테이블 (corp_mast_history)
-- 설명: 법인 정보 변경 및 처리 이력을 추적하는 테이블
-- =====================================================================================
CREATE TABLE IF NOT EXISTS corp_mast_history (
    id BIGSERIAL PRIMARY KEY,                                      -- 이력 고유 ID
    corp_mast_id BIGINT,                                          -- 법인 마스터 ID (외래키, NULL 허용)
    username VARCHAR(100),                                         -- 작업 수행자
    action VARCHAR(50),                                            -- 수행 액션 (INSERT, UPDATE, DELETE, API_CALL 등)
    biz_no VARCHAR(20),                                           -- 관련 사업자번호
    result VARCHAR(20),                                           -- 처리 결과 (SUCCESS, FAIL, ERROR 등)
    message TEXT,                                                 -- 상세 메시지
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,       -- 처리 시간
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,     -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,     -- 수정일시
    
    -- 외래키 제약조건 (NULL 허용)
    CONSTRAINT fk_corp_mast_history_corp_mast FOREIGN KEY (corp_mast_id) REFERENCES corp_mast(id) ON DELETE SET NULL,
    
    -- 제약조건
    CONSTRAINT corp_mast_history_action_check CHECK (action IN ('INSERT', 'UPDATE', 'DELETE', 'API_CALL', 'BATCH_PROCESS', 'VALIDATION')),
    CONSTRAINT corp_mast_history_result_check CHECK (result IN ('SUCCESS', 'FAIL', 'ERROR', 'WARNING', 'PENDING'))
);

-- 법인 마스터 이력 테이블 코멘트
COMMENT ON TABLE corp_mast_history IS '법인 정보 변경 및 처리 이력 추적 테이블';
COMMENT ON COLUMN corp_mast_history.id IS '이력 고유 ID';
COMMENT ON COLUMN corp_mast_history.corp_mast_id IS '관련 법인 마스터 ID (삭제시 NULL)';
COMMENT ON COLUMN corp_mast_history.username IS '작업 수행자 사용자명';
COMMENT ON COLUMN corp_mast_history.action IS '수행된 액션 종류 (INSERT, UPDATE, DELETE, API_CALL, BATCH_PROCESS, VALIDATION)';
COMMENT ON COLUMN corp_mast_history.biz_no IS '관련 사업자번호';
COMMENT ON COLUMN corp_mast_history.result IS '처리 결과 (SUCCESS, FAIL, ERROR, WARNING, PENDING)';
COMMENT ON COLUMN corp_mast_history.message IS '상세 처리 메시지 또는 오류 내용';
COMMENT ON COLUMN corp_mast_history.timestamp IS '작업 수행 시간';
COMMENT ON COLUMN corp_mast_history.create_date IS '이력 생성일시';
COMMENT ON COLUMN corp_mast_history.modify_date IS '이력 수정일시';

-- =====================================================================================
-- 5. 파일 정보 테이블 (files)
-- 설명: 업로드된 파일의 메타데이터 및 정보를 관리하는 테이블
-- =====================================================================================
CREATE TABLE IF NOT EXISTS files (
    id BIGSERIAL PRIMARY KEY,                                      -- 파일 고유 ID
    original_file_name VARCHAR(255) NOT NULL,                      -- 원본 파일명
    stored_file_name VARCHAR(255) NOT NULL UNIQUE,                 -- 저장된 파일명 (고유)
    content_type VARCHAR(100) NOT NULL,                            -- 파일 MIME 타입
    file_size BIGINT NOT NULL,                                     -- 파일 크기 (바이트)
    description VARCHAR(1000),                                     -- 파일 설명
    upload_time TIMESTAMP NOT NULL,                               -- 업로드 시간
    last_modified_time TIMESTAMP NOT NULL,                        -- 마지막 수정 시간
    uploader_id BIGINT,                                           -- 업로더 회원 ID (외래키)
    uploader_name VARCHAR(50),                                     -- 업로더명
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 수정일시
    
    -- 외래키 제약조건
    CONSTRAINT fk_files_uploader FOREIGN KEY (uploader_id) REFERENCES members(id) ON DELETE SET NULL,
    
    -- 제약조건
    CONSTRAINT files_file_size_check CHECK (file_size > 0 AND file_size <= 104857600), -- 최대 100MB
    CONSTRAINT files_content_type_check CHECK (content_type IS NOT NULL AND LENGTH(content_type) > 0)
);

-- 파일 정보 테이블 코멘트
COMMENT ON TABLE files IS '업로드된 파일의 메타데이터 및 관리 정보 테이블';
COMMENT ON COLUMN files.id IS '파일 고유 ID';
COMMENT ON COLUMN files.original_file_name IS '사용자가 업로드한 원본 파일명';
COMMENT ON COLUMN files.stored_file_name IS '서버에 저장된 파일명 (UUID 기반, 고유값)';
COMMENT ON COLUMN files.content_type IS '파일 MIME 타입 (예: image/jpeg, text/csv)';
COMMENT ON COLUMN files.file_size IS '파일 크기 (바이트 단위, 최대 100MB)';
COMMENT ON COLUMN files.description IS '파일에 대한 설명 (최대 1000자)';
COMMENT ON COLUMN files.upload_time IS '파일 업로드 시간';
COMMENT ON COLUMN files.last_modified_time IS '파일 정보 마지막 수정 시간';
COMMENT ON COLUMN files.uploader_id IS '파일을 업로드한 회원 ID';
COMMENT ON COLUMN files.uploader_name IS '업로더 이름 (캐시용)';
COMMENT ON COLUMN files.create_date IS '레코드 생성일시';
COMMENT ON COLUMN files.modify_date IS '레코드 수정일시';

-- =====================================================================================
-- 6. CSV 배치 처리 이력 테이블 (csv_batch_history)
-- 설명: CSV 파일의 배치 처리 이력을 관리하는 테이블
-- =====================================================================================
CREATE TABLE IF NOT EXISTS csv_batch_history (
    id BIGSERIAL PRIMARY KEY,                                      -- 배치 이력 고유 ID
    city VARCHAR(50),                                              -- 처리 대상 시/도
    district VARCHAR(50),                                          -- 처리 대상 시군구
    file_name VARCHAR(255),                                        -- 처리된 CSV 파일명
    record_count INTEGER DEFAULT 0,                               -- 처리된 레코드 수
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',                 -- 처리 상태
    message TEXT,                                                  -- 처리 메시지
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,        -- 처리 시간
    
    -- 제약조건
    CONSTRAINT csv_batch_history_status_check CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAIL', 'ERROR')),
    CONSTRAINT csv_batch_history_record_count_check CHECK (record_count >= 0)
);

-- CSV 배치 처리 이력 테이블 코멘트
COMMENT ON TABLE csv_batch_history IS 'CSV 파일 배치 처리 이력 관리 테이블';
COMMENT ON COLUMN csv_batch_history.id IS '배치 처리 이력 고유 ID';
COMMENT ON COLUMN csv_batch_history.city IS '처리 대상 시/도명';
COMMENT ON COLUMN csv_batch_history.district IS '처리 대상 시군구명';
COMMENT ON COLUMN csv_batch_history.file_name IS '처리된 CSV 파일명';
COMMENT ON COLUMN csv_batch_history.record_count IS '처리된 레코드 수';
COMMENT ON COLUMN csv_batch_history.status IS '처리 상태 (PENDING, PROCESSING, SUCCESS, FAIL, ERROR)';
COMMENT ON COLUMN csv_batch_history.message IS '처리 결과 메시지 또는 오류 내용';
COMMENT ON COLUMN csv_batch_history.timestamp IS '배치 처리 시간';

-- =====================================================================================
-- 성능 최적화를 위한 인덱스 생성
-- =====================================================================================

-- 회원 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_members_username ON members(username);                    -- 로그인 조회용
CREATE INDEX IF NOT EXISTS idx_members_email ON members(email);                          -- 이메일 조회용  
CREATE INDEX IF NOT EXISTS idx_members_api_key ON members(api_key);                      -- API 키 조회용
CREATE INDEX IF NOT EXISTS idx_members_status ON members(status);                        -- 상태별 조회용
CREATE INDEX IF NOT EXISTS idx_members_role ON members(role);                            -- 권한별 조회용
CREATE INDEX IF NOT EXISTS idx_members_status_role ON members(status, role);             -- 상태+권한 복합 조회용
CREATE INDEX IF NOT EXISTS idx_members_create_date ON members(create_date);              -- 가입일 정렬용
CREATE INDEX IF NOT EXISTS idx_members_last_login_at ON members(last_login_at);          -- 마지막 로그인 정렬용
CREATE INDEX IF NOT EXISTS idx_members_approved_by ON members(approved_by);              -- 승인자별 조회용
CREATE INDEX IF NOT EXISTS idx_members_account_locked ON members(account_locked_at) WHERE account_locked_at IS NOT NULL; -- 잠김 계정 조회용

-- 회원 비밀번호 이력 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_member_password_history_member_id ON member_password_history(member_id);      -- 회원별 이력 조회용
CREATE INDEX IF NOT EXISTS idx_member_password_history_created_at ON member_password_history(created_at);    -- 생성일 정렬용
CREATE INDEX IF NOT EXISTS idx_member_password_history_member_created ON member_password_history(member_id, created_at); -- 회원별 최신 이력 조회용

-- 법인 마스터 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_corp_mast_biz_no ON corp_mast(biz_no);                    -- 사업자번호 조회용 (이미 UNIQUE로 인덱스 존재하지만 명시적 생성)
CREATE INDEX IF NOT EXISTS idx_corp_mast_corp_reg_no ON corp_mast(corp_reg_no);          -- 법인등록번호 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_seller_id ON corp_mast(seller_id);              -- 판매자ID 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_username ON corp_mast(username);                -- 등록자별 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_location ON corp_mast(si_nm, sgg_nm);           -- 지역별 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_region_cd ON corp_mast(region_cd);              -- 지역코드 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_biz_nm ON corp_mast(biz_nm);                    -- 사업자명 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_create_date ON corp_mast(create_date);          -- 생성일 정렬용
CREATE INDEX IF NOT EXISTS idx_corp_mast_modify_date ON corp_mast(modify_date);          -- 수정일 정렬용
CREATE INDEX IF NOT EXISTS idx_corp_mast_biz_nm_gin ON corp_mast USING gin(to_tsvector('korean', biz_nm)); -- 사업자명 전문검색용

-- 법인 마스터 이력 테이블 인덱스  
CREATE INDEX IF NOT EXISTS idx_corp_mast_history_corp_mast_id ON corp_mast_history(corp_mast_id);   -- 법인별 이력 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_history_username ON corp_mast_history(username);           -- 작업자별 이력 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_history_action ON corp_mast_history(action);               -- 액션별 이력 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_history_result ON corp_mast_history(result);               -- 결과별 이력 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_history_biz_no ON corp_mast_history(biz_no);               -- 사업자번호별 이력 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_history_timestamp ON corp_mast_history(timestamp);         -- 시간순 정렬용
CREATE INDEX IF NOT EXISTS idx_corp_mast_history_action_result ON corp_mast_history(action, result); -- 액션+결과 복합 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_history_username_timestamp ON corp_mast_history(username, timestamp); -- 사용자별 시간순 조회용

-- 파일 정보 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_files_stored_file_name ON files(stored_file_name);        -- 저장 파일명 조회용 (이미 UNIQUE로 인덱스 존재)
CREATE INDEX IF NOT EXISTS idx_files_uploader_id ON files(uploader_id);                  -- 업로더별 조회용
CREATE INDEX IF NOT EXISTS idx_files_content_type ON files(content_type);                -- 파일 타입별 조회용
CREATE INDEX IF NOT EXISTS idx_files_upload_time ON files(upload_time);                  -- 업로드 시간 정렬용
CREATE INDEX IF NOT EXISTS idx_files_file_size ON files(file_size);                      -- 파일 크기별 조회용
CREATE INDEX IF NOT EXISTS idx_files_uploader_upload_time ON files(uploader_id, upload_time); -- 업로더별 시간순 조회용

-- CSV 배치 처리 이력 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_csv_batch_history_city_district ON csv_batch_history(city, district); -- 지역별 배치 이력 조회용
CREATE INDEX IF NOT EXISTS idx_csv_batch_history_status ON csv_batch_history(status);                 -- 상태별 조회용
CREATE INDEX IF NOT EXISTS idx_csv_batch_history_timestamp ON csv_batch_history(timestamp);           -- 시간순 정렬용
CREATE INDEX IF NOT EXISTS idx_csv_batch_history_file_name ON csv_batch_history(file_name);           -- 파일명별 조회용

-- =====================================================================================
-- 트리거 함수 생성 (modify_date 자동 업데이트용)
-- =====================================================================================
CREATE OR REPLACE FUNCTION update_modify_date()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modify_date = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 트리거 생성 (각 테이블의 modify_date 자동 업데이트)
CREATE TRIGGER trigger_members_update_modify_date
    BEFORE UPDATE ON members
    FOR EACH ROW
    EXECUTE FUNCTION update_modify_date();

CREATE TRIGGER trigger_corp_mast_update_modify_date
    BEFORE UPDATE ON corp_mast
    FOR EACH ROW
    EXECUTE FUNCTION update_modify_date();

CREATE TRIGGER trigger_corp_mast_history_update_modify_date
    BEFORE UPDATE ON corp_mast_history
    FOR EACH ROW
    EXECUTE FUNCTION update_modify_date();

CREATE TRIGGER trigger_files_update_modify_date
    BEFORE UPDATE ON files
    FOR EACH ROW
    EXECUTE FUNCTION update_modify_date();

-- =====================================================================================
-- 기본 데이터 INSERT (관리자 계정 및 시스템 기본 데이터)
-- =====================================================================================

-- 기본 관리자 계정 생성 (비밀번호: admin123!@#)
INSERT INTO members (username, password, nickname, email, api_key, status, role, password_changed_at)
VALUES 
    ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '시스템관리자', 'admin@company.com', 
     'admin-api-key-' || substr(md5(random()::text), 1, 32), 'APPROVED', 'ADMIN', CURRENT_TIMESTAMP),
    ('manager', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '운영관리자', 'manager@company.com',
     'manager-api-key-' || substr(md5(random()::text), 1, 32), 'APPROVED', 'MANAGER', CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- =====================================================================================
-- 더미 데이터 생성 (1000건의 테스트 데이터)
-- =====================================================================================

-- 일반 사용자 더미 데이터 (100명)
INSERT INTO members (username, password, nickname, email, api_key, status, role, password_changed_at, last_login_at)
SELECT 
    'user' || i,
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',  -- password123
    '사용자' || i,
    'user' || i || '@test.com',
    'api-key-' || substr(md5(('user' || i || random()::text)::text), 1, 32),
    CASE 
        WHEN i % 10 = 0 THEN 'PENDING'
        WHEN i % 15 = 0 THEN 'REJECTED' 
        WHEN i % 20 = 0 THEN 'SUSPENDED'
        ELSE 'APPROVED'
    END,
    CASE 
        WHEN i % 25 = 0 THEN 'MANAGER'
        ELSE 'USER'
    END,
    CURRENT_TIMESTAMP - (random() * interval '365 days'),
    CASE 
        WHEN random() > 0.3 THEN CURRENT_TIMESTAMP - (random() * interval '30 days')
        ELSE NULL
    END
FROM generate_series(1, 100) i
ON CONFLICT (username) DO NOTHING;

-- 시/도 및 시군구 배열 정의 (더미 데이터용)
WITH regions AS (
    SELECT * FROM (
        VALUES 
            ('서울특별시', '강남구'), ('서울특별시', '강동구'), ('서울특별시', '강북구'), ('서울특별시', '강서구'),
            ('서울특별시', '관악구'), ('서울특별시', '광진구'), ('서울특별시', '구로구'), ('서울특별시', '금천구'),
            ('부산광역시', '중구'), ('부산광역시', '서구'), ('부산광역시', '동구'), ('부산광역시', '영도구'),
            ('부산광역시', '부산진구'), ('부산광역시', '동래구'), ('부산광역시', '남구'), ('부산광역시', '북구'),
            ('대구광역시', '중구'), ('대구광역시', '동구'), ('대구광역시', '서구'), ('대구광역시', '남구'),
            ('대구광역시', '북구'), ('대구광역시', '수성구'), ('대구광역시', '달서구'), ('대구광역시', '달성군'),
            ('인천광역시', '중구'), ('인천광역시', '동구'), ('인천광역시', '미추홀구'), ('인천광역시', '연수구'),
            ('인천광역시', '남동구'), ('인천광역시', '부평구'), ('인천광역시', '계양구'), ('인천광역시', '서구'),
            ('광주광역시', '동구'), ('광주광역시', '서구'), ('광주광역시', '남구'), ('광주광역시', '북구'),
            ('대전광역시', '동구'), ('대전광역시', '중구'), ('대전광역시', '서구'), ('대전광역시', '유성구'),
            ('울산광역시', '중구'), ('울산광역시', '남구'), ('울산광역시', '동구'), ('울산광역시', '북구'),
            ('경기도', '수원시'), ('경기도', '성남시'), ('경기도', '안양시'), ('경기도', '안산시'),
            ('경기도', '고양시'), ('경기도', '과천시'), ('경기도', '구리시'), ('경기도', '남양주시'),
            ('강원도', '춘천시'), ('강원도', '원주시'), ('강원도', '강릉시'), ('강원도', '동해시'),
            ('충청북도', '청주시'), ('충청북도', '충주시'), ('충청북도', '제천시'), ('충청북도', '보은군'),
            ('충청남도', '천안시'), ('충청남도', '공주시'), ('충청남도', '보령시'), ('충청남도', '아산시'),
            ('전라북도', '전주시'), ('전라북도', '군산시'), ('전라북도', '익산시'), ('전라북도', '정읍시'),
            ('전라남도', '목포시'), ('전라남도', '여수시'), ('전라남도', '순천시'), ('전라남도', '나주시'),
            ('경상북도', '포항시'), ('경상북도', '경주시'), ('경상북도', '김천시'), ('경상북도', '안동시'),
            ('경상남도', '창원시'), ('경상남도', '진주시'), ('경상남도', '통영시'), ('경상남도', '사천시'),
            ('제주특별자치도', '제주시'), ('제주특별자치도', '서귀포시')
    ) AS t(si_nm, sgg_nm)
),
company_types AS (
    SELECT * FROM (
        VALUES 
            ('(주)', '주식회사'), ('(유)', '유한회사'), ('(합)', '합명회사'), ('(합자)', '합자회사'),
            ('', '개인사업자'), ('(사)', '사단법인'), ('(재)', '재단법인'), ('(협)', '협동조합')
    ) AS t(prefix, suffix)
),
business_names AS (
    SELECT * FROM (
        VALUES 
            ('테크놀로지', '솔루션', '시스템', '네트워크', '소프트웨어', '하드웨어', '데이터', '클라우드'),
            ('마케팅', '컨설팅', '서비스', '매니지먼트', '비즈니스', '엔터프라이즈', '글로벌', '이노베이션'),
            ('커뮤니케이션', '인터랙티브', '디지털', '모바일', '웹', '앱', '플랫폼', '인터페이스'),
            ('로지스틱스', '트레이딩', '임포트', '익스포트', '매뉴팩처링', '프로덕션', '디스트리뷰션', '리테일'),
            ('헬스케어', '바이오', '메디컬', '파마', '웰니스', '피트니스', '뷰티', '코스메틱'),
            ('에듀케이션', '트레이닝', '아카데미', '인스티튜트', '스쿨', '칼리지', '유니버시티', '센터'),
            ('엔터테인먼트', '미디어', '브로드캐스팅', '퍼블리싱', '프로덕션', '스튜디오', '에이전시', '크리에이티브'),
            ('파이낸셜', '인베스트먼트', '캐피탈', '펀드', '어셋', '인슈어런스', '뱅킹', '세큐리티'),
            ('컨스트럭션', '엔지니어링', '아키텍처', '디자인', '플래닝', '데벨롭먼트', '인프라', '빌딩'),
            ('에너지', '파워', '유틸리티', '리소스', '오일', '가스', '솔라', '윈드'),
            ('오토모티브', '트랜스포트', '로지스틱', '쉬핑', '항공', '철도', '해운', '택배'),
            ('푸드', '레스토랑', '카페', '베이커리', '델리', '케이터링', '호텔', '리조트')
    ) AS t(name)
)

-- 법인 마스터 더미 데이터 생성 (900건)
INSERT INTO corp_mast (seller_id, biz_nm, biz_no, corp_reg_no, region_cd, si_nm, sgg_nm, username, description)
SELECT 
    '2024-' || substring(r.si_nm, 1, 2) || substring(r.sgg_nm, 1, 2) || '-' || lpad((i % 9999 + 1)::text, 5, '0'),
    CASE 
        WHEN i % 3 = 0 THEN ct.prefix || bn.name || ct.suffix
        WHEN i % 3 = 1 THEN bn.name || ct.prefix
        ELSE ct.prefix || bn.name
    END,
    -- 사업자번호 생성 (XXX-XX-XXXXX 형식)
    lpad(((i * 17 + 100) % 900 + 100)::text, 3, '0') || '-' || 
    lpad(((i * 23 + 10) % 90 + 10)::text, 2, '0') || '-' ||
    lpad(((i * 31 + 10000) % 90000 + 10000)::text, 5, '0'),
    -- 법인등록번호 생성 (XXXXXX-XXXXXXX 형식)
    lpad(((i * 41 + 110111) % 900000 + 100000)::text, 6, '0') || '-' ||
    lpad(((i * 47 + 1000000) % 9000000 + 1000000)::text, 7, '0'),
    -- 지역코드 생성
    lpad(((i * 13) % 9000 + 1000)::text, 4, '0') || '0' || 
    lpad(((i * 19) % 900 + 100)::text, 3, '0') || '00',
    r.si_nm,
    r.sgg_nm,
    CASE 
        WHEN i % 50 = 0 THEN 'admin'
        WHEN i % 25 = 0 THEN 'manager'
        ELSE 'user' || ((i % 100) + 1)
    END,
    CASE 
        WHEN i % 10 = 0 THEN bn.name || ' 관련 사업을 영위하는 법인입니다. 설립일: ' || (CURRENT_DATE - (random() * interval '10 years'))::date
        WHEN i % 7 = 0 THEN '전국 단위의 ' || bn.name || ' 서비스를 제공합니다.'
        WHEN i % 5 = 0 THEN r.si_nm || ' ' || r.sgg_nm || ' 지역의 ' || bn.name || ' 전문 업체입니다.'
        ELSE NULL
    END
FROM 
    generate_series(1, 900) i,
    (SELECT *, row_number() OVER () as rn FROM regions) r,
    (SELECT *, row_number() OVER () as rn FROM company_types) ct,
    (SELECT *, row_number() OVER () as rn FROM business_names) bn
WHERE 
    r.rn = (i % (SELECT count(*) FROM regions)) + 1
    AND ct.rn = (i % (SELECT count(*) FROM company_types)) + 1
    AND bn.rn = (i % (SELECT count(*) FROM business_names)) + 1
ON CONFLICT (biz_no) DO NOTHING;

-- 법인 마스터 이력 더미 데이터 생성
INSERT INTO corp_mast_history (corp_mast_id, username, action, biz_no, result, message, timestamp)
SELECT 
    cm.id,
    cm.username,
    CASE 
        WHEN i % 10 = 0 THEN 'UPDATE'
        WHEN i % 15 = 0 THEN 'API_CALL'
        WHEN i % 20 = 0 THEN 'VALIDATION'
        ELSE 'INSERT'
    END,
    cm.biz_no,
    CASE 
        WHEN i % 12 = 0 THEN 'FAIL'
        WHEN i % 25 = 0 THEN 'WARNING'
        WHEN i % 30 = 0 THEN 'ERROR'
        ELSE 'SUCCESS'
    END,
    CASE 
        WHEN i % 12 = 0 THEN '데이터 검증 실패: 필수 항목 누락'
        WHEN i % 25 = 0 THEN '중복 가능성 있는 사업자번호 발견'
        WHEN i % 30 = 0 THEN 'API 호출 시간 초과'
        ELSE '정상 처리됨'
    END,
    CURRENT_TIMESTAMP - (random() * interval '180 days')
FROM 
    corp_mast cm,
    generate_series(1, 3) i
WHERE cm.id <= 300; -- 각 법인당 최대 3개의 이력

-- 회원 비밀번호 이력 더미 데이터 생성
INSERT INTO member_password_history (member_id, password_hash, created_at)
SELECT 
    m.id,
    '$2a$10$' || substr(md5((m.id || i || random()::text)::text), 1, 53),
    CURRENT_TIMESTAMP - (random() * interval '365 days') - (i * interval '30 days')
FROM 
    members m,
    generate_series(1, 3) i
WHERE m.id <= 50; -- 처음 50명의 회원에 대해 비밀번호 이력 생성

-- 파일 정보 더미 데이터 생성
INSERT INTO files (original_file_name, stored_file_name, content_type, file_size, description, upload_time, last_modified_time, uploader_id, uploader_name)
SELECT 
    CASE 
        WHEN i % 5 = 0 THEN 'corp_data_' || i || '.csv'
        WHEN i % 5 = 1 THEN 'report_' || i || '.pdf'
        WHEN i % 5 = 2 THEN 'image_' || i || '.jpg'
        WHEN i % 5 = 3 THEN 'document_' || i || '.docx'
        ELSE 'excel_' || i || '.xlsx'
    END,
    gen_random_uuid()::text || CASE 
        WHEN i % 5 = 0 THEN '.csv'
        WHEN i % 5 = 1 THEN '.pdf'
        WHEN i % 5 = 2 THEN '.jpg'
        WHEN i % 5 = 3 THEN '.docx'
        ELSE '.xlsx'
    END,
    CASE 
        WHEN i % 5 = 0 THEN 'text/csv'
        WHEN i % 5 = 1 THEN 'application/pdf'
        WHEN i % 5 = 2 THEN 'image/jpeg'
        WHEN i % 5 = 3 THEN 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        ELSE 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    END,
    (random() * 10000000 + 1000)::bigint, -- 1KB ~ 10MB
    CASE 
        WHEN i % 3 = 0 THEN '법인 정보 관련 파일입니다.'
        WHEN i % 3 = 1 THEN '시스템 보고서 파일입니다.'
        ELSE '업무용 문서 파일입니다.'
    END,
    CURRENT_TIMESTAMP - (random() * interval '365 days'),
    CURRENT_TIMESTAMP - (random() * interval '180 days'),
    ((i % 50) + 1), -- 처음 50명의 회원 중에서 업로더 선택
    'user' || ((i % 50) + 1)
FROM generate_series(1, 150) i;

-- CSV 배치 처리 이력 더미 데이터 생성
INSERT INTO csv_batch_history (city, district, file_name, record_count, status, message, timestamp)
SELECT 
    r.si_nm,
    r.sgg_nm,
    'batch_' || r.si_nm || '_' || r.sgg_nm || '_' || to_char(CURRENT_DATE - (i * interval '1 day'), 'YYYYMMDD') || '.csv',
    (random() * 1000 + 10)::integer,
    CASE 
        WHEN i % 10 = 0 THEN 'FAIL'
        WHEN i % 15 = 0 THEN 'ERROR'
        WHEN i % 20 = 0 THEN 'PROCESSING'
        ELSE 'SUCCESS'
    END,
    CASE 
        WHEN i % 10 = 0 THEN 'CSV 파일 형식 오류'
        WHEN i % 15 = 0 THEN '외부 API 연결 실패'
        WHEN i % 20 = 0 THEN '배치 처리 진행 중'
        ELSE '정상 처리 완료'
    END,
    CURRENT_TIMESTAMP - (i * interval '1 day')
FROM 
    (SELECT *, row_number() OVER () as rn FROM regions) r,
    generate_series(1, 30) i
WHERE r.rn <= 20; -- 처음 20개 지역에 대해 30일간의 배치 이력 생성

-- =====================================================================================
-- 통계 및 분석용 뷰 생성
-- =====================================================================================

-- 회원 통계 뷰
CREATE OR REPLACE VIEW v_member_statistics AS
SELECT 
    status,
    role,
    COUNT(*) as member_count,
    COUNT(CASE WHEN last_login_at >= CURRENT_DATE - INTERVAL '30 days' THEN 1 END) as active_last_30days,
    COUNT(CASE WHEN create_date >= CURRENT_DATE - INTERVAL '30 days' THEN 1 END) as new_last_30days,
    AVG(login_fail_count) as avg_login_fail_count
FROM members 
GROUP BY status, role
ORDER BY status, role;

COMMENT ON VIEW v_member_statistics IS '회원 상태 및 권한별 통계 뷰';

-- 법인 지역별 통계 뷰  
CREATE OR REPLACE VIEW v_corp_region_statistics AS
SELECT 
    si_nm,
    sgg_nm,
    COUNT(*) as corp_count,
    COUNT(DISTINCT username) as registered_by_count,
    MIN(create_date) as first_registered_at,
    MAX(create_date) as last_registered_at,
    COUNT(CASE WHEN create_date >= CURRENT_DATE - INTERVAL '30 days' THEN 1 END) as new_last_30days
FROM corp_mast 
GROUP BY si_nm, sgg_nm
ORDER BY corp_count DESC, si_nm, sgg_nm;

COMMENT ON VIEW v_corp_region_statistics IS '법인 정보 지역별 통계 뷰';

-- 일일 활동 통계 뷰
CREATE OR REPLACE VIEW v_daily_activity_statistics AS
SELECT 
    activity_date,
    new_members,
    new_corps,
    file_uploads,
    csv_batches,
    total_activities
FROM (
    SELECT 
        date_trunc('day', activity_date) as activity_date,
        SUM(CASE WHEN activity_type = 'MEMBER' THEN 1 ELSE 0 END) as new_members,
        SUM(CASE WHEN activity_type = 'CORP' THEN 1 ELSE 0 END) as new_corps,
        SUM(CASE WHEN activity_type = 'FILE' THEN 1 ELSE 0 END) as file_uploads,
        SUM(CASE WHEN activity_type = 'CSV_BATCH' THEN 1 ELSE 0 END) as csv_batches,
        COUNT(*) as total_activities
    FROM (
        SELECT create_date as activity_date, 'MEMBER' as activity_type FROM members
        UNION ALL
        SELECT create_date as activity_date, 'CORP' as activity_type FROM corp_mast
        UNION ALL 
        SELECT upload_time as activity_date, 'FILE' as activity_type FROM files
        UNION ALL
        SELECT timestamp as activity_date, 'CSV_BATCH' as activity_type FROM csv_batch_history
    ) activities
    GROUP BY date_trunc('day', activity_date)
) daily_stats
ORDER BY activity_date DESC;

COMMENT ON VIEW v_daily_activity_statistics IS '일별 시스템 활동 통계 뷰';

-- =====================================================================================
-- 데이터 정합성 검증 쿼리
-- =====================================================================================

-- 데이터 정합성 검증 결과 출력
SELECT 
    '=== 데이터베이스 생성 완료 ===' as message,
    CURRENT_TIMESTAMP as completed_at;

SELECT 
    'members' as table_name,
    COUNT(*) as record_count,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved_count,
    COUNT(CASE WHEN role = 'ADMIN' THEN 1 END) as admin_count
FROM members
UNION ALL
SELECT 
    'corp_mast' as table_name,
    COUNT(*) as record_count,
    COUNT(DISTINCT si_nm) as distinct_cities,
    COUNT(DISTINCT username) as distinct_users
FROM corp_mast
UNION ALL
SELECT 
    'corp_mast_history' as table_name,
    COUNT(*) as record_count,
    COUNT(CASE WHEN result = 'SUCCESS' THEN 1 END) as success_count,
    COUNT(CASE WHEN action = 'INSERT' THEN 1 END) as insert_count
FROM corp_mast_history
UNION ALL
SELECT 
    'files' as table_name,
    COUNT(*) as record_count,
    COUNT(CASE WHEN content_type LIKE 'text/%' THEN 1 END) as text_files,
    COUNT(CASE WHEN uploader_id IS NOT NULL THEN 1 END) as with_uploader
FROM files
UNION ALL
SELECT 
    'csv_batch_history' as table_name,
    COUNT(*) as record_count,
    COUNT(CASE WHEN status = 'SUCCESS' THEN 1 END) as success_count,
    COUNT(DISTINCT city) as distinct_cities
FROM csv_batch_history;

-- =====================================================================================
-- 스크립트 실행 완료 메시지
-- =====================================================================================
SELECT 
    '=== PostgreSQL 데이터베이스 스크립트 실행 완료 ===' as message,
    '총 ' || (
        (SELECT COUNT(*) FROM members) +
        (SELECT COUNT(*) FROM corp_mast) + 
        (SELECT COUNT(*) FROM corp_mast_history) +
        (SELECT COUNT(*) FROM files) +
        (SELECT COUNT(*) FROM csv_batch_history)
    ) || '건의 데이터가 생성되었습니다.' as summary,
    CURRENT_TIMESTAMP as completed_at;

-- 성능 통계 업데이트
ANALYZE members;
ANALYZE corp_mast;
ANALYZE corp_mast_history;
ANALYZE files;
ANALYZE csv_batch_history;
ANALYZE member_password_history;

-- =====================================================================================
-- 끝
-- =====================================================================================
