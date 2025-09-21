-- =====================================================================================
-- 디버깅용 SQL 쿼리 모음
-- 법인 정보 필드 누락 문제 해결을 위한 데이터베이스 조사
-- =====================================================================================

-- 1. 울산광역시 중구 법인 데이터 확인
SELECT 
    id,
    biz_nm,
    rep_nm,
    estb_dt,
    road_nm_addr,
    jibun_addr,
    corp_status,
    si_nm,
    sgg_nm,
    create_date
FROM corp_mast 
WHERE si_nm = '울산광역시' AND sgg_nm = '중구'
ORDER BY id
LIMIT 10;

-- 2. 세종특별자치시 법인 데이터 확인
SELECT 
    id,
    biz_nm,
    rep_nm,
    estb_dt,
    road_nm_addr,
    jibun_addr,
    corp_status,
    si_nm,
    sgg_nm,
    create_date
FROM corp_mast 
WHERE si_nm = '세종특별자치시' AND sgg_nm = '세종특별자치시'
ORDER BY id
LIMIT 10;

-- 3. 새로운 컬럼들이 NULL인 데이터 확인
SELECT 
    COUNT(*) as total_count,
    COUNT(CASE WHEN rep_nm IS NOT NULL AND rep_nm != '' THEN 1 END) as has_rep_nm,
    COUNT(CASE WHEN estb_dt IS NOT NULL AND estb_dt != '' THEN 1 END) as has_estb_dt,
    COUNT(CASE WHEN road_nm_addr IS NOT NULL AND road_nm_addr != '' THEN 1 END) as has_road_addr,
    COUNT(CASE WHEN jibun_addr IS NOT NULL AND jibun_addr != '' THEN 1 END) as has_jibun_addr,
    COUNT(CASE WHEN corp_status IS NOT NULL AND corp_status != '' THEN 1 END) as has_corp_status
FROM corp_mast;

-- 4. 지역별 새로운 필드 데이터 현황
SELECT 
    si_nm,
    sgg_nm,
    COUNT(*) as total_count,
    COUNT(CASE WHEN rep_nm IS NOT NULL AND rep_nm != '' THEN 1 END) as has_rep_nm,
    COUNT(CASE WHEN estb_dt IS NOT NULL AND estb_dt != '' THEN 1 END) as has_estb_dt,
    COUNT(CASE WHEN road_nm_addr IS NOT NULL AND road_nm_addr != '' THEN 1 END) as has_road_addr,
    COUNT(CASE WHEN corp_status IS NOT NULL AND corp_status != '' THEN 1 END) as has_corp_status
FROM corp_mast 
GROUP BY si_nm, sgg_nm
ORDER BY total_count DESC
LIMIT 20;

-- 5. 컬럼 존재 여부 확인
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'corp_mast' 
AND column_name IN ('rep_nm', 'estb_dt', 'road_nm_addr', 'jibun_addr', 'corp_status')
ORDER BY column_name;

-- 6. 최근 생성된 데이터 확인 (새로운 필드가 있는지)
SELECT 
    id,
    biz_nm,
    rep_nm,
    estb_dt,
    road_nm_addr,
    corp_status,
    create_date
FROM corp_mast 
ORDER BY create_date DESC
LIMIT 10;

-- 7. 테이블 구조 확인
\d corp_mast;

-- 8. 샘플 데이터 업데이트 (테스트용)
UPDATE corp_mast 
SET 
    rep_nm = '김대표',
    estb_dt = '20200101',
    road_nm_addr = '울산광역시 중구 성남동 123-45',
    jibun_addr = '울산광역시 중구 성남동 123-45',
    corp_status = '계속(수익)'
WHERE si_nm = '울산광역시' AND sgg_nm = '중구'
AND id IN (SELECT id FROM corp_mast WHERE si_nm = '울산광역시' AND sgg_nm = '중구' LIMIT 5);

-- 9. 업데이트 후 확인
SELECT 
    id,
    biz_nm,
    rep_nm,
    estb_dt,
    road_nm_addr,
    corp_status
FROM corp_mast 
WHERE si_nm = '울산광역시' AND sgg_nm = '중구'
ORDER BY id
LIMIT 10;
