-- =====================================================================================
-- 누락된 컬럼 추가 스크립트
-- rep_nm, estb_dt, corp_status, road_nm_addr, jibun_addr 컬럼을 corp_mast 테이블에 추가
-- =====================================================================================

-- 1. 현재 테이블 구조 확인
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'corp_mast' 
ORDER BY ordinal_position;

-- 2. 누락된 컬럼들 추가
ALTER TABLE corp_mast 
ADD COLUMN IF NOT EXISTS rep_nm VARCHAR(100),           -- 대표자명
ADD COLUMN IF NOT EXISTS estb_dt VARCHAR(20),           -- 설립일자
ADD COLUMN IF NOT EXISTS road_nm_addr VARCHAR(200),     -- 도로명주소
ADD COLUMN IF NOT EXISTS jibun_addr VARCHAR(200),       -- 지번주소
ADD COLUMN IF NOT EXISTS corp_status VARCHAR(50);       -- 법인상태

-- 3. 컬럼 추가 후 확인
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'corp_mast' 
AND column_name IN ('rep_nm', 'estb_dt', 'road_nm_addr', 'jibun_addr', 'corp_status')
ORDER BY column_name;

-- 4. 테이블 구조 전체 확인
\d corp_mast;

-- 5. 샘플 데이터 추가 (울산광역시 중구)
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
        WHEN id % 7 = 0 THEN '계속(수익)'
        WHEN id % 7 = 1 THEN '휴업'
        WHEN id % 7 = 2 THEN '폐업'
        WHEN id % 7 = 3 THEN '계속(수익)'
        WHEN id % 7 = 4 THEN '계속(수익)'
        WHEN id % 7 = 5 THEN '휴업'
        ELSE '계속(수익)'
    END
WHERE si_nm = '울산광역시' AND sgg_nm = '중구';

-- 6. 샘플 데이터 추가 (세종특별자치시)
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
        WHEN id % 7 = 0 THEN '계속(수익)'
        WHEN id % 7 = 1 THEN '휴업'
        WHEN id % 7 = 2 THEN '폐업'
        WHEN id % 7 = 3 THEN '계속(수익)'
        WHEN id % 7 = 4 THEN '계속(수익)'
        WHEN id % 7 = 5 THEN '휴업'
        ELSE '계속(수익)'
    END
WHERE si_nm = '세종특별자치시' AND sgg_nm = '세종특별자치시';

-- 7. 업데이트 결과 확인
SELECT 
    '울산광역시 중구' as region,
    COUNT(*) as total_count,
    COUNT(CASE WHEN rep_nm IS NOT NULL AND rep_nm != '' THEN 1 END) as has_rep_nm,
    COUNT(CASE WHEN estb_dt IS NOT NULL AND estb_dt != '' THEN 1 END) as has_estb_dt,
    COUNT(CASE WHEN road_nm_addr IS NOT NULL AND road_nm_addr != '' THEN 1 END) as has_road_addr,
    COUNT(CASE WHEN corp_status IS NOT NULL AND corp_status != '' THEN 1 END) as has_corp_status
FROM corp_mast 
WHERE si_nm = '울산광역시' AND sgg_nm = '중구'

UNION ALL

SELECT 
    '세종특별자치시' as region,
    COUNT(*) as total_count,
    COUNT(CASE WHEN rep_nm IS NOT NULL AND rep_nm != '' THEN 1 END) as has_rep_nm,
    COUNT(CASE WHEN estb_dt IS NOT NULL AND estb_dt != '' THEN 1 END) as has_estb_dt,
    COUNT(CASE WHEN road_nm_addr IS NOT NULL AND road_nm_addr != '' THEN 1 END) as has_road_addr,
    COUNT(CASE WHEN corp_status IS NOT NULL AND corp_status != '' THEN 1 END) as has_corp_status
FROM corp_mast 
WHERE si_nm = '세종특별자치시' AND sgg_nm = '세종특별자치시';

-- 8. 샘플 데이터 확인
SELECT 
    id,
    biz_nm,
    rep_nm,
    estb_dt,
    road_nm_addr,
    corp_status,
    si_nm,
    sgg_nm
FROM corp_mast 
WHERE (si_nm = '울산광역시' AND sgg_nm = '중구') 
   OR (si_nm = '세종특별자치시' AND sgg_nm = '세종특별자치시')
ORDER BY si_nm, sgg_nm, id
LIMIT 10;
