-- =============================================
-- 댓글 테이블 스키마 수정
-- 생성일: 2025-07-25
-- 목적: Comment 엔티티와 DB 스키마 일치시키기
-- =============================================

-- 1. 기존 COMMENTS 테이블 백업 (혹시 모를 데이터 손실 방지)
CREATE TABLE COMMENTS_BACKUP AS SELECT * FROM COMMENTS;

-- 2. 기존 COMMENTS 테이블 구조 확인 및 수정

-- 먼저 현재 테이블 구조 확인
DESC COMMENTS;

-- 3. 필요한 컬럼 수정/추가

-- CONTENT 컬럼을 COMMENT_TEXT로 변경하지 말고, 엔티티에서 CONTENT를 사용하도록 수정했음
-- 따라서 여기서는 RESULT_ID를 NULL 허용으로 변경만 함

-- RESULT_ID를 NULL 허용으로 변경 (커뮤니티 댓글은 특정 결과와 연관되지 않을 수 있음)
ALTER TABLE COMMENTS MODIFY (RESULT_ID NUMBER NULL);

-- 4. USER_ID 컬럼이 없다면 추가 (사용자 시스템용)
-- 주석: 기존 스키마에 USER_ID가 없을 수 있으므로 체크 후 추가
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE COMMENTS ADD (USER_ID NUMBER)';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1430 THEN -- 컬럼이 이미 존재
            NULL;
        ELSE
            RAISE;
        END IF;
END;
/

-- 5. 외래키 제약조건을 사용자 테이블에 추가 (선택사항)
-- ALTER TABLE COMMENTS ADD CONSTRAINT FK_COMMENTS_USER FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID);

-- 6. 인덱스 생성/재생성
CREATE INDEX IDX_COMMENTS_USER_ID ON COMMENTS(USER_ID);
CREATE INDEX IDX_COMMENTS_MBTI_TYPE ON COMMENTS(MBTI_TYPE);
CREATE INDEX IDX_COMMENTS_CREATED_AT ON COMMENTS(CREATED_AT);

-- 7. 현재 테이블 구조 확인
DESC COMMENTS;

-- 8. 샘플 댓글 데이터 삽입 (테스트용)
-- 기존 테스트 결과가 있는지 확인
SELECT COUNT(*) as result_count FROM TEST_RESULTS;

-- 샘플 테스트 결과가 없다면 하나 생성
INSERT INTO TEST_RESULTS (
    USER_UUID, MBTI_TYPE, DETAILED_SCORES, AI_ANALYSIS, TEST_ANSWERS
) 
SELECT 'sample-uuid-' || ROWNUM, 'ENFP', '{}', 'Sample analysis', '[]'
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM TEST_RESULTS)
AND ROWNUM = 1;

-- 샘플 댓글 추가 (resultId가 있는 경우와 없는 경우 모두)
INSERT INTO COMMENTS (RESULT_ID, USER_UUID, NICKNAME, MBTI_TYPE, CONTENT, LIKES_COUNT, IS_DELETED) VALUES
(1, 'sample-uuid-1', '테스터1', 'ENFP', '첫 번째 테스트 댓글입니다!', 0, 0);

-- 커뮤니티 댓글 (resultId가 null인 경우)
INSERT INTO COMMENTS (RESULT_ID, USER_UUID, NICKNAME, MBTI_TYPE, CONTENT, LIKES_COUNT, IS_DELETED) VALUES
(NULL, 'sample-uuid-2', '커뮤니티러', 'INFP', '안녕하세요! INFP 타입입니다. 같은 타입 분들 반가워요!', 5, 0);

INSERT INTO COMMENTS (RESULT_ID, USER_UUID, NICKNAME, MBTI_TYPE, CONTENT, LIKES_COUNT, IS_DELETED) VALUES
(NULL, 'sample-uuid-3', 'MBTI탐험가', 'ENTP', 'ENTP는 정말 토론을 좋아하는 것 같아요. 여러분은 어떠신가요?', 3, 0);

COMMIT;

-- 9. 수정 완료 확인
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    NULLABLE,
    DATA_DEFAULT
FROM USER_TAB_COLUMNS 
WHERE TABLE_NAME = 'COMMENTS'
ORDER BY COLUMN_ID;

-- 10. 샘플 데이터 확인
SELECT 
    COMMENT_ID,
    RESULT_ID,
    NICKNAME,
    MBTI_TYPE,
    SUBSTR(CONTENT, 1, 50) AS CONTENT_PREVIEW,
    LIKES_COUNT,
    IS_DELETED,
    CREATED_AT
FROM COMMENTS
ORDER BY CREATED_AT DESC;

PROMPT '댓글 테이블 스키마 수정이 완료되었습니다!'
PROMPT '- RESULT_ID: NULL 허용으로 변경'
PROMPT '- USER_ID: 컬럼 추가 (이미 있으면 무시)'
PROMPT '- 인덱스: 성능 최적화용 인덱스 추가'
PROMPT '- 샘플 데이터: 테스트용 댓글 추가'
