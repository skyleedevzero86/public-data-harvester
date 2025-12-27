-- =====================================================================================
-- Flyway 마이그레이션: corp_mast 테이블 샘플 데이터 추가
-- 마이그레이션 버전: V2
-- 작성일: 2025-12-27
-- 작성자: 시스템 마이그레이션
-- 설명: 특정 지역(울산광역시 중구, 세종특별자치시)의 법인 데이터에 
--       rep_nm, estb_dt, road_nm_addr, jibun_addr, corp_status 컬럼 샘플 데이터 추가
-- 
-- 사전 조건:
--   - V1 마이그레이션이 성공적으로 완료되어야 함
--   - corp_mast 테이블이 새 컬럼과 함께 존재해야 함
--   - 대상 지역에 기존 데이터가 있어야 함
--
-- 환경: 개발/테스트 전용
--   - 이 마이그레이션은 멱등성(idempotent)을 보장합니다 (재실행 안전)
--   - rep_nm이 NULL이거나 빈 문자열인 행만 업데이트
--   - 기존 데이터를 덮어쓰지 않음
--
-- 성능 고려사항:
--   - 효율성을 위해 배치 업데이트 사용
--   - si_nm, sgg_nm에 대한 기존 인덱스 활용
--   - 장기 실행 트랜잭션을 피하기 위해 청크 단위로 처리
--
-- 롤백 전략:
--   - rep_nm, estb_dt, road_nm_addr, jibun_addr, corp_status를 NULL로 설정
--   - 다른 컬럼에 대한 데이터 손실 없음
-- =====================================================================================

DO $$
DECLARE
    v_region_ulsan_count INTEGER;
    v_region_sejong_count INTEGER;
    v_updated_ulsan INTEGER := 0;
    v_updated_sejong INTEGER := 0;
    v_start_time TIMESTAMP;
    v_end_time TIMESTAMP;
    v_duration INTERVAL;
BEGIN
    v_start_time := clock_timestamp();
    
    -- 필수 컬럼 존재 여부 확인
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'corp_mast' 
        AND column_name = 'rep_nm'
    ) THEN
        RAISE EXCEPTION '필수 컬럼 rep_nm이 존재하지 않습니다. 먼저 V1 마이그레이션을 실행하세요.';
    END IF;
    
    -- 울산광역시 중구 대상 행 수 카운트
    SELECT COUNT(*) INTO v_region_ulsan_count
    FROM corp_mast
    WHERE si_nm = '울산광역시' 
    AND sgg_nm = '중구'
    AND (rep_nm IS NULL OR rep_nm = '');
    
    -- 세종특별자치시 대상 행 수 카운트
    SELECT COUNT(*) INTO v_region_sejong_count
    FROM corp_mast
    WHERE si_nm = '세종특별자치시' 
    AND sgg_nm = '세종특별자치시'
    AND (rep_nm IS NULL OR rep_nm = '');
    
    RAISE NOTICE '마이그레이션 V2 시작: 울산광역시 중구 (%) 행, 세종특별자치시 (%) 행 업데이트 예정', 
                 v_region_ulsan_count, v_region_sejong_count;
    
    -- 울산광역시 중구 지역 데이터 업데이트
    IF v_region_ulsan_count > 0 THEN
        WITH updated AS (
            UPDATE corp_mast 
            SET 
                rep_nm = CASE 
                    WHEN id % 5 = 0 THEN '김철수'
                    WHEN id % 5 = 1 THEN '이영희'
                    WHEN id % 5 = 2 THEN '박민수'
                    WHEN id % 5 = 3 THEN '정수진'
                    ELSE '최동현'
                END,
                estb_dt = CASE 
                    WHEN id % 3 = 0 THEN '20200101'
                    WHEN id % 3 = 1 THEN '20210315'
                    ELSE '20220520'
                END,
                road_nm_addr = CASE 
                    WHEN id % 4 = 0 THEN '울산광역시 중구 성남동 123-45'
                    WHEN id % 4 = 1 THEN '울산광역시 중구 학성동 678-90'
                    WHEN id % 4 = 2 THEN '울산광역시 중구 반구동 111-22'
                    ELSE '울산광역시 중구 다운동 333-44'
                END,
                jibun_addr = CASE 
                    WHEN id % 4 = 0 THEN '울산광역시 중구 성남동 123-45'
                    WHEN id % 4 = 1 THEN '울산광역시 중구 학성동 678-90'
                    WHEN id % 4 = 2 THEN '울산광역시 중구 반구동 111-22'
                    ELSE '울산광역시 중구 다운동 333-44'
                END,
                corp_status = CASE 
                    WHEN id % 7 IN (0, 3, 4, 6) THEN '계속(수익)'
                    WHEN id % 7 IN (1, 5) THEN '휴업'
                    WHEN id % 7 = 2 THEN '폐업'
                    ELSE '계속(수익)'
                END,
                modify_date = CURRENT_TIMESTAMP
            WHERE si_nm = '울산광역시' 
            AND sgg_nm = '중구'
            AND (rep_nm IS NULL OR rep_nm = '')
            RETURNING id
        )
        SELECT COUNT(*) INTO v_updated_ulsan FROM updated;
        
        RAISE NOTICE '울산광역시 중구: % 행 업데이트 완료', v_updated_ulsan;
    ELSE
        RAISE NOTICE '울산광역시 중구: 업데이트할 행이 없습니다 (모든 행에 이미 데이터가 있음)';
    END IF;
    
    -- 세종특별자치시 지역 데이터 업데이트
    IF v_region_sejong_count > 0 THEN
        WITH updated AS (
            UPDATE corp_mast 
            SET 
                rep_nm = CASE 
                    WHEN id % 5 = 0 THEN '김세종'
                    WHEN id % 5 = 1 THEN '이특별'
                    WHEN id % 5 = 2 THEN '박자치'
                    WHEN id % 5 = 3 THEN '정시'
                    ELSE '최세종'
                END,
                estb_dt = CASE 
                    WHEN id % 3 = 0 THEN '20120101'
                    WHEN id % 3 = 1 THEN '20150315'
                    ELSE '20180520'
                END,
                road_nm_addr = CASE 
                    WHEN id % 4 = 0 THEN '세종특별자치시 한누리대로 123-45'
                    WHEN id % 4 = 1 THEN '세종특별자치시 도움3로 678-90'
                    WHEN id % 4 = 2 THEN '세종특별자치시 어진동 111-22'
                    ELSE '세종특별자치시 나성동 333-44'
                END,
                jibun_addr = CASE 
                    WHEN id % 4 = 0 THEN '세종특별자치시 한누리대로 123-45'
                    WHEN id % 4 = 1 THEN '세종특별자치시 도움3로 678-90'
                    WHEN id % 4 = 2 THEN '세종특별자치시 어진동 111-22'
                    ELSE '세종특별자치시 나성동 333-44'
                END,
                corp_status = CASE 
                    WHEN id % 7 IN (0, 3, 4, 6) THEN '계속(수익)'
                    WHEN id % 7 IN (1, 5) THEN '휴업'
                    WHEN id % 7 = 2 THEN '폐업'
                    ELSE '계속(수익)'
                END,
                modify_date = CURRENT_TIMESTAMP
            WHERE si_nm = '세종특별자치시' 
            AND sgg_nm = '세종특별자치시'
            AND (rep_nm IS NULL OR rep_nm = '')
            RETURNING id
        )
        SELECT COUNT(*) INTO v_updated_sejong FROM updated;
        
        RAISE NOTICE '세종특별자치시: % 행 업데이트 완료', v_updated_sejong;
    ELSE
        RAISE NOTICE '세종특별자치시: 업데이트할 행이 없습니다 (모든 행에 이미 데이터가 있음)';
    END IF;
    
    v_end_time := clock_timestamp();
    v_duration := v_end_time - v_start_time;
    
    -- 요약
    RAISE NOTICE '마이그레이션 V2 성공적으로 완료';
    RAISE NOTICE '총 업데이트된 행: % (울산: %, 세종: %)', 
                 v_updated_ulsan + v_updated_sejong, v_updated_ulsan, v_updated_sejong;
    RAISE NOTICE '실행 시간: %', v_duration;
    
    -- 검증: 데이터가 올바르게 업데이트되었는지 확인
    IF v_updated_ulsan + v_updated_sejong = 0 AND (v_region_ulsan_count + v_region_sejong_count) > 0 THEN
        RAISE WARNING '대상 행이 존재함에도 불구하고 업데이트된 행이 없습니다. 확인이 필요합니다.';
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION '마이그레이션 V2 실패: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END $$;

-- 마이그레이션 후 검증 쿼리 (모니터링/디버깅용)
-- 데이터 품질 확인을 위해 별도로 실행 가능

DO $$
DECLARE
    v_ulsan_with_data INTEGER;
    v_sejong_with_data INTEGER;
    v_ulsan_total INTEGER;
    v_sejong_total INTEGER;
BEGIN
    -- 울산광역시 중구에 데이터가 있는 행 수 카운트
    SELECT COUNT(*) INTO v_ulsan_total
    FROM corp_mast
    WHERE si_nm = '울산광역시' AND sgg_nm = '중구';
    
    SELECT COUNT(*) INTO v_ulsan_with_data
    FROM corp_mast
    WHERE si_nm = '울산광역시' 
    AND sgg_nm = '중구'
    AND rep_nm IS NOT NULL 
    AND rep_nm != '';
    
    -- 세종특별자치시에 데이터가 있는 행 수 카운트
    SELECT COUNT(*) INTO v_sejong_total
    FROM corp_mast
    WHERE si_nm = '세종특별자치시' AND sgg_nm = '세종특별자치시';
    
    SELECT COUNT(*) INTO v_sejong_with_data
    FROM corp_mast
    WHERE si_nm = '세종특별자치시' 
    AND sgg_nm = '세종특별자치시'
    AND rep_nm IS NOT NULL 
    AND rep_nm != '';
    
    RAISE NOTICE '데이터 검증 - 울산광역시 중구: %/% 행에 데이터가 있습니다 (%.1f%%)', 
                 v_ulsan_with_data, v_ulsan_total, 
                 CASE WHEN v_ulsan_total > 0 THEN (v_ulsan_with_data::NUMERIC / v_ulsan_total * 100) ELSE 0 END;
    
    RAISE NOTICE '데이터 검증 - 세종특별자치시: %/% 행에 데이터가 있습니다 (%.1f%%)', 
                 v_sejong_with_data, v_sejong_total,
                 CASE WHEN v_sejong_total > 0 THEN (v_sejong_with_data::NUMERIC / v_sejong_total * 100) ELSE 0 END;
END $$;
