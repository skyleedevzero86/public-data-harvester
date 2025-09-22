-- =====================================================================================
-- system_health 테이블 생성 스크립트
-- 프로젝트: Public Data Harvester J-Backend
-- 작성일: 2025-09-22
-- 설명: 시스템 헬스 상태를 관리하는 테이블
-- =====================================================================================

-- system_health 테이블 생성
CREATE TABLE IF NOT EXISTS system_health (
    id BIGSERIAL PRIMARY KEY,                                      -- 시스템 헬스 ID
    overall_status VARCHAR(20) NOT NULL,                           -- 전체 상태 (UP, DOWN, UNKNOWN)
    total_components INTEGER,                                      -- 총 컴포넌트 수
    healthy_components INTEGER,                                    -- 정상 컴포넌트 수
    unhealthy_components INTEGER,                                  -- 장애 컴포넌트 수
    unknown_components INTEGER,                                    -- 알 수 없는 컴포넌트 수
    details VARCHAR(2000),                                         -- 상세 정보 (JSON)
    checked_at TIMESTAMP,                                          -- 체크 실행 시간
    expires_at TIMESTAMP,                                          -- 만료 시간
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 수정일시
    
    -- 제약조건
    CONSTRAINT system_health_status_check CHECK (overall_status IN ('UP', 'DOWN', 'UNKNOWN')),
    CONSTRAINT system_health_total_components_check CHECK (total_components >= 0),
    CONSTRAINT system_health_healthy_components_check CHECK (healthy_components >= 0),
    CONSTRAINT system_health_unhealthy_components_check CHECK (unhealthy_components >= 0),
    CONSTRAINT system_health_unknown_components_check CHECK (unknown_components >= 0)
);

-- system_health 테이블 코멘트
COMMENT ON TABLE system_health IS '전체 시스템 헬스 상태 관리 테이블';
COMMENT ON COLUMN system_health.id IS '시스템 헬스 ID (자동증가)';
COMMENT ON COLUMN system_health.overall_status IS '전체 상태 (UP:정상, DOWN:장애, UNKNOWN:알수없음)';
COMMENT ON COLUMN system_health.total_components IS '총 컴포넌트 수';
COMMENT ON COLUMN system_health.healthy_components IS '정상 컴포넌트 수';
COMMENT ON COLUMN system_health.unhealthy_components IS '장애 컴포넌트 수';
COMMENT ON COLUMN system_health.unknown_components IS '알 수 없는 컴포넌트 수';
COMMENT ON COLUMN system_health.details IS '상세 정보 (JSON 형식)';
COMMENT ON COLUMN system_health.checked_at IS '체크 실행 시간';
COMMENT ON COLUMN system_health.expires_at IS '만료 시간';
COMMENT ON COLUMN system_health.create_date IS '레코드 생성일시';
COMMENT ON COLUMN system_health.modify_date IS '레코드 수정일시';

-- system_health 테이블 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_system_health_create_date ON system_health(create_date);
CREATE INDEX IF NOT EXISTS idx_system_health_overall_status ON system_health(overall_status);
CREATE INDEX IF NOT EXISTS idx_system_health_checked_at ON system_health(checked_at);
CREATE INDEX IF NOT EXISTS idx_system_health_expires_at ON system_health(expires_at);

-- system_health 테이블에 트리거 추가 (modify_date 자동 업데이트)
CREATE TRIGGER trigger_system_health_update_modify_date
    BEFORE UPDATE ON system_health
    FOR EACH ROW
    EXECUTE FUNCTION update_modify_date();

-- =====================================================================================
-- health_checks 테이블 생성 (기존에 없을 경우)
-- =====================================================================================

CREATE TABLE IF NOT EXISTS health_checks (
    id BIGSERIAL PRIMARY KEY,                                      -- 헬스 체크 ID
    check_type VARCHAR(50) NOT NULL,                               -- 체크 타입
    component VARCHAR(100) NOT NULL,                               -- 컴포넌트명
    status VARCHAR(20) NOT NULL,                                   -- 상태 (UP, DOWN, UNKNOWN)
    message VARCHAR(500),                                          -- 메시지
    details VARCHAR(2000),                                         -- 상세 정보 (JSON)
    response_time BIGINT,                                          -- 응답 시간 (ms)
    checked_at TIMESTAMP NOT NULL,                                 -- 체크 시간
    expires_at TIMESTAMP,                                          -- 만료 시간
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 생성일시
    modify_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- 수정일시
    
    -- 제약조건
    CONSTRAINT health_checks_status_check CHECK (status IN ('UP', 'DOWN', 'UNKNOWN')),
    CONSTRAINT health_checks_response_time_check CHECK (response_time >= 0)
);

-- health_checks 테이블 코멘트
COMMENT ON TABLE health_checks IS '개별 컴포넌트 헬스 체크 결과 관리 테이블';
COMMENT ON COLUMN health_checks.id IS '헬스 체크 ID (자동증가)';
COMMENT ON COLUMN health_checks.check_type IS '체크 타입 (DATABASE, REDIS, API, EXTERNAL 등)';
COMMENT ON COLUMN health_checks.component IS '컴포넌트명';
COMMENT ON COLUMN health_checks.status IS '상태 (UP:정상, DOWN:장애, UNKNOWN:알수없음)';
COMMENT ON COLUMN health_checks.message IS '상태 메시지';
COMMENT ON COLUMN health_checks.details IS '상세 정보 (JSON 형식)';
COMMENT ON COLUMN health_checks.response_time IS '응답 시간 (밀리초)';
COMMENT ON COLUMN health_checks.checked_at IS '체크 실행 시간';
COMMENT ON COLUMN health_checks.expires_at IS '만료 시간';
COMMENT ON COLUMN health_checks.create_date IS '레코드 생성일시';
COMMENT ON COLUMN health_checks.modify_date IS '레코드 수정일시';

-- health_checks 테이블 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_health_checks_checked_at ON health_checks(checked_at);
CREATE INDEX IF NOT EXISTS idx_health_checks_component ON health_checks(component);
CREATE INDEX IF NOT EXISTS idx_health_checks_status ON health_checks(status);
CREATE INDEX IF NOT EXISTS idx_health_checks_check_type ON health_checks(check_type);
CREATE INDEX IF NOT EXISTS idx_health_checks_expires_at ON health_checks(expires_at);

-- health_checks 테이블에 트리거 추가 (modify_date 자동 업데이트)
CREATE TRIGGER trigger_health_checks_update_modify_date
    BEFORE UPDATE ON health_checks
    FOR EACH ROW
    EXECUTE FUNCTION update_modify_date();

-- =====================================================================================
-- 스크립트 실행 완료 메시지
-- =====================================================================================
SELECT 
    '=== system_health 및 health_checks 테이블 생성 완료 ===' as message,
    CURRENT_TIMESTAMP as completed_at;
