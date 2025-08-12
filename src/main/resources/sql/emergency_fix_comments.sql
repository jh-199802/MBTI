-- 응급 수정: COMMENTS 테이블의 RESULT_ID NULL 허용
-- 이 스크립트를 Oracle DB에서 먼저 실행해야 합니다.

-- 1. 현재 제약조건 확인
SELECT constraint_name, constraint_type, search_condition, status
FROM user_constraints 
WHERE table_name = 'COMMENTS' AND constraint_type = 'C';

-- 2. RESULT_ID NOT NULL 제약조건 찾기 및 제거
SELECT constraint_name 
FROM user_constraints 
WHERE table_name = 'COMMENTS' 
AND constraint_type = 'C' 
AND search_condition LIKE '%RESULT_ID%IS NOT NULL%';

-- 3. RESULT_ID를 NULL 허용으로 변경
ALTER TABLE COMMENTS MODIFY RESULT_ID NUMBER(10) NULL;

-- 4. 확인
DESC COMMENTS;

-- 변경 완료 후 COMMIT
COMMIT;
