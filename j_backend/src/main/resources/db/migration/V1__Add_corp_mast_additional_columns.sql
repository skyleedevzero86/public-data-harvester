-- =====================================================================================
-- Flyway 마이그레이션: corp_mast 추가 컬럼
-- 프로젝트: Public Data Harvester J-Backend
-- 마이그레이션 버전: V1
-- 작성일: 2025-12-27
-- 설명: 법인 마스터에 대표자명, 설립일자, 주소, 법인상태 컬럼을 추가한다
-- =====================================================================================

-- =====================================================================================
-- 1. corp_mast 추가 컬럼
-- 설명: 테이블이 있을 때만 없는 컬럼을 추가하고 컬럼 코멘트를 남긴다
-- =====================================================================================

DO $$
DECLARE
    v_table_exists BOOLEAN;
    v_columns_added INTEGER := 0;
BEGIN
    -- 테이블 존재 여부 확인
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'corp_mast'
    ) INTO v_table_exists;
    
    IF NOT v_table_exists THEN
        RAISE EXCEPTION 'corp_mast 테이블이 존재하지 않습니다. 마이그레이션을 진행할 수 없습니다.';
    END IF;
    
    -- rep_nm 컬럼 추가 (대표자명)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'corp_mast' 
        AND column_name = 'rep_nm'
    ) THEN
        ALTER TABLE corp_mast 
        ADD COLUMN rep_nm VARCHAR(100);
        
        COMMENT ON COLUMN corp_mast.rep_nm IS '대표자명 (법인 대표자 이름)';
        v_columns_added := v_columns_added + 1;
    END IF;
    
    -- estb_dt 컬럼 추가 (설립일자)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'corp_mast' 
        AND column_name = 'estb_dt'
    ) THEN
        ALTER TABLE corp_mast 
        ADD COLUMN estb_dt VARCHAR(20);
        
        COMMENT ON COLUMN corp_mast.estb_dt IS '설립일자 (YYYYMMDD 형식)';
        v_columns_added := v_columns_added + 1;
    END IF;
    
    -- road_nm_addr 컬럼 추가 (도로명주소)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'corp_mast' 
        AND column_name = 'road_nm_addr'
    ) THEN
        ALTER TABLE corp_mast 
        ADD COLUMN road_nm_addr VARCHAR(200);
        
        COMMENT ON COLUMN corp_mast.road_nm_addr IS '도로명주소 (새주소 체계)';
        v_columns_added := v_columns_added + 1;
    END IF;
    
    -- jibun_addr 컬럼 추가 (지번주소)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'corp_mast' 
        AND column_name = 'jibun_addr'
    ) THEN
        ALTER TABLE corp_mast 
        ADD COLUMN jibun_addr VARCHAR(200);
        
        COMMENT ON COLUMN corp_mast.jibun_addr IS '지번주소 (구주소 체계)';
        v_columns_added := v_columns_added + 1;
    END IF;
    
    -- corp_status 컬럼 추가 (법인상태)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'corp_mast' 
        AND column_name = 'corp_status'
    ) THEN
        ALTER TABLE corp_mast 
        ADD COLUMN corp_status VARCHAR(50);
        
        COMMENT ON COLUMN corp_mast.corp_status IS '법인상태 (예: 계속(수익), 휴업, 폐업)';
        v_columns_added := v_columns_added + 1;
    END IF;
    
    -- 마이그레이션 결과 로깅
    RAISE NOTICE '마이그레이션 V1 완료: corp_mast 테이블에 % 개의 컬럼이 추가되었습니다', v_columns_added;
    
    -- 모든 컬럼이 성공적으로 추가되었는지 확인
    IF v_columns_added < 5 THEN
        RAISE WARNING '예상된 5개의 컬럼 중 % 개만 추가되었습니다. 일부 컬럼이 이미 존재할 수 있습니다.', v_columns_added;
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION '마이그레이션 V1 실패: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END $$;

-- =====================================================================================
-- 2. 인덱스
-- 설명: 추가 컬럼 조회용. 값이 있는 행만 담는다
-- =====================================================================================

CREATE INDEX IF NOT EXISTS idx_corp_mast_rep_nm ON corp_mast(rep_nm) WHERE rep_nm IS NOT NULL;           -- 대표자명 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_corp_status ON corp_mast(corp_status) WHERE corp_status IS NOT NULL; -- 법인상태 조회용
CREATE INDEX IF NOT EXISTS idx_corp_mast_estb_dt ON corp_mast(estb_dt) WHERE estb_dt IS NOT NULL;         -- 설립일자 조회용

-- =====================================================================================
-- 3. 검증
-- 설명: 다섯 컬럼이 모두 있는지 확인한다
-- =====================================================================================
DO $$
DECLARE
    v_expected_columns TEXT[] := ARRAY['rep_nm', 'estb_dt', 'road_nm_addr', 'jibun_addr', 'corp_status'];
    v_missing_columns TEXT[];
    v_column TEXT;
BEGIN
    FOREACH v_column IN ARRAY v_expected_columns
    LOOP
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_schema = 'public' 
            AND table_name = 'corp_mast' 
            AND column_name = v_column
        ) THEN
            v_missing_columns := array_append(v_missing_columns, v_column);
        END IF;
    END LOOP;
    
    IF array_length(v_missing_columns, 1) > 0 THEN
        RAISE EXCEPTION '마이그레이션 검증 실패. 누락된 컬럼: %', array_to_string(v_missing_columns, ', ');
    END IF;
    
    RAISE NOTICE '마이그레이션 V1 검증 통과: 모든 컬럼이 존재합니다';
END $$;
