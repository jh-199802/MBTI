-- =============================================
-- MBTI 커뮤니티 댓글 시스템 완전 설정 스크립트
-- 생성일: 2025-07-25
-- 목적: 커뮤니티 댓글 작성 문제 해결
-- =============================================

-- 현재 연결된 사용자 확인
SELECT USER FROM DUAL;

-- 현재 존재하는 테이블 확인
SELECT TABLE_NAME FROM USER_TABLES 
WHERE TABLE_NAME IN ('COMMENTS', 'TEST_RESULTS', 'MBTI_USERS') 
ORDER BY TABLE_NAME;

-- =============================================
-- 1단계: 사용자 시스템 설정
-- =============================================

-- 사용자 시퀀스가 없으면 생성
DECLARE
    seq_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO seq_count 
    FROM USER_SEQUENCES 
    WHERE SEQUENCE_NAME = 'SEQ_MBTI_USER';
    
    IF seq_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_MBTI_USER START WITH 1 INCREMENT BY 1 NOMAXVALUE NOCYCLE CACHE 20';
    END IF;
END;
/

-- 사용자 테이블이 없으면 생성
DECLARE
    table_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO table_count 
    FROM USER_TABLES 
    WHERE TABLE_NAME = 'MBTI_USERS';
    
    IF table_count = 0 THEN
        EXECUTE IMMEDIATE '
        CREATE TABLE MBTI_USERS (
            USER_ID NUMBER(20) PRIMARY KEY,
            USERNAME VARCHAR2(50) NOT NULL UNIQUE,
            EMAIL VARCHAR2(100) NOT NULL UNIQUE,
            PASSWORD_HASH VARCHAR2(255) NOT NULL,
            NICKNAME VARCHAR2(50),
            PROFILE_IMAGE VARCHAR2(500),
            MBTI_TYPE VARCHAR2(4),
            IS_ACTIVE CHAR(1) DEFAULT ''Y'' CHECK (IS_ACTIVE IN (''Y'', ''N'')),
            CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            LAST_LOGIN TIMESTAMP
        )';
    END IF;
END;
/

-- =============================================
-- 2단계: 댓글 테이블 구조 확인 및 수정
-- =============================================

-- 현재 COMMENTS 테이블 구조 확인
DESC COMMENTS;

-- RESULT_ID를 NULL 허용으로 변경 (커뮤니티 댓글용)
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE COMMENTS MODIFY RESULT_ID NUMBER NULL';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1442 THEN -- 이미 nullable이 아닌 경우만 에러
            RAISE;
        END IF;
END;
/

-- USER_ID 컬럼이 없으면 추가
DECLARE
    column_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO column_count 
    FROM USER_TAB_COLUMNS 
    WHERE TABLE_NAME = 'COMMENTS' AND COLUMN_NAME = 'USER_ID';
    
    IF column_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE COMMENTS ADD USER_ID NUMBER(20)';
    END IF;
END;
/

-- =============================================
-- 3단계: 테스트 결과 테이블에 USER_ID 추가
-- =============================================

-- TEST_RESULTS 테이블에 USER_ID 컬럼이 없으면 추가
DECLARE
    column_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO column_count 
    FROM USER_TAB_COLUMNS 
    WHERE TABLE_NAME = 'TEST_RESULTS' AND COLUMN_NAME = 'USER_ID';
    
    IF column_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE TEST_RESULTS ADD USER_ID NUMBER(20)';
    END IF;
END;
/

-- =============================================
-- 4단계: 시퀀스 확인 및 생성
-- =============================================

-- 댓글 시퀀스가 없으면 생성
DECLARE
    seq_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO seq_count 
    FROM USER_SEQUENCES 
    WHERE SEQUENCE_NAME = 'SEQ_COMMENT';
    
    IF seq_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_COMMENT START WITH 1 INCREMENT BY 1 NOMAXVALUE NOCYCLE CACHE 20';
    END IF;
END;
/

-- 테스트 결과 시퀀스가 없으면 생성
DECLARE
    seq_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO seq_count 
    FROM USER_SEQUENCES 
    WHERE SEQUENCE_NAME = 'SEQ_TEST_RESULT';
    
    IF seq_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_TEST_RESULT START WITH 1 INCREMENT BY 1 NOMAXVALUE NOCYCLE CACHE 20';
    END IF;
END;
/

-- =============================================
-- 5단계: 인덱스 생성 (성능 최적화)
-- =============================================

-- 인덱스 중복 생성 방지를 위한 프로시저
CREATE OR REPLACE PROCEDURE CREATE_INDEX_IF_NOT_EXISTS(
    p_index_name VARCHAR2,
    p_table_name VARCHAR2,
    p_column_name VARCHAR2
) AS
    index_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO index_count 
    FROM USER_INDEXES 
    WHERE INDEX_NAME = p_index_name;
    
    IF index_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX ' || p_index_name || ' ON ' || p_table_name || '(' || p_column_name || ')';
    END IF;
END;
/

-- 필요한 인덱스들 생성
BEGIN
    CREATE_INDEX_IF_NOT_EXISTS('IDX_COMMENTS_USER_ID', 'COMMENTS', 'USER_ID');
    CREATE_INDEX_IF_NOT_EXISTS('IDX_COMMENTS_MBTI_TYPE', 'COMMENTS', 'MBTI_TYPE');
    CREATE_INDEX_IF_NOT_EXISTS('IDX_COMMENTS_CREATED_AT', 'COMMENTS', 'CREATED_AT');
    CREATE_INDEX_IF_NOT_EXISTS('IDX_COMMENTS_RESULT_ID', 'COMMENTS', 'RESULT_ID');
    CREATE_INDEX_IF_NOT_EXISTS('IDX_TEST_RESULTS_USER_ID', 'TEST_RESULTS', 'USER_ID');
    CREATE_INDEX_IF_NOT_EXISTS('IDX_MBTI_USERS_USERNAME', 'MBTI_USERS', 'USERNAME');
    CREATE_INDEX_IF_NOT_EXISTS('IDX_MBTI_USERS_EMAIL', 'MBTI_USERS', 'EMAIL');
END;
/

-- 프로시저 정리
DROP PROCEDURE CREATE_INDEX_IF_NOT_EXISTS;

-- =============================================
-- 6단계: 샘플 데이터 추가
-- =============================================

-- 샘플 테스트 결과가 없으면 하나 생성
DECLARE
    result_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO result_count FROM TEST_RESULTS;
    
    IF result_count = 0 THEN
        INSERT INTO TEST_RESULTS (
            RESULT_ID, USER_UUID, MBTI_TYPE, DETAILED_SCORES, AI_ANALYSIS, TEST_ANSWERS
        ) VALUES (
            SEQ_TEST_RESULT.NEXTVAL,
            'sample-uuid-community',
            'ENFP',
            '{"E":70,"I":30,"S":40,"N":60,"T":35,"F":65,"J":25,"P":75}',
            'ENFP 타입으로 분석되었습니다. 활발하고 열정적인 성격입니다.',
            '[1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1]'
        );
    END IF;
END;
/

-- 샘플 사용자가 없으면 생성
DECLARE
    user_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM MBTI_USERS;
    
    IF user_count = 0 THEN
        INSERT INTO MBTI_USERS (
            USER_ID, USERNAME, EMAIL, PASSWORD_HASH, NICKNAME, MBTI_TYPE
        ) VALUES (
            SEQ_MBTI_USER.NEXTVAL,
            'testuser',
            'test@example.com',
            '$2a$10$dummyhashfortest',
            '테스트유저',
            'ENFP'
        );
    END IF;
END;
/

-- 샘플 커뮤니티 댓글 추가 (resultId가 null인 댓글)
DECLARE
    comment_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO comment_count 
    FROM COMMENTS 
    WHERE RESULT_ID IS NULL;
    
    IF comment_count = 0 THEN
        -- 커뮤니티 댓글들 (특정 테스트 결과와 연관되지 않음)
        INSERT INTO COMMENTS (COMMENT_ID, RESULT_ID, USER_UUID, NICKNAME, MBTI_TYPE, CONTENT, LIKES_COUNT, IS_DELETED) VALUES
        (SEQ_COMMENT.NEXTVAL, NULL, 'guest-user-1', '익명의ENFP', 'ENFP', '안녕하세요! ENFP 타입입니다. 같은 유형 분들과 이야기하고 싶어요!', 5, 'N');
        
        INSERT INTO COMMENTS (COMMENT_ID, RESULT_ID, USER_UUID, NICKNAME, MBTI_TYPE, CONTENT, LIKES_COUNT, IS_DELETED) VALUES
        (SEQ_COMMENT.NEXTVAL, NULL, 'guest-user-2', 'INTJ탐험가', 'INTJ', 'INTJ는 혼자 있는 시간이 정말 소중하죠. 여러분은 어떤 방식으로 에너지를 충전하시나요?', 8, 'N');
        
        INSERT INTO COMMENTS (COMMENT_ID, RESULT_ID, USER_UUID, NICKNAME, MBTI_TYPE, CONTENT, LIKES_COUNT, IS_DELETED) VALUES
        (SEQ_COMMENT.NEXTVAL, NULL, 'guest-user-3', '감성적인INFP', 'INFP', '오늘 날씨가 너무 좋네요~ INFP인 저는 이런 날씨에 감성이 충만해져요 🌸', 12, 'N');
        
        INSERT INTO COMMENTS (COMMENT_ID, RESULT_ID, USER_UUID, NICKNAME, MBTI_TYPE, CONTENT, LIKES_COUNT, IS_DELETED) VALUES
        (SEQ_COMMENT.NEXTVAL, NULL, 'guest-user-4', 'ESTP액티브', 'ESTP', 'ESTP는 정말 액션이 필요해요! 집에만 있으면 답답해서 못 견디겠어요. 같은 분들 있나요?', 6, 'N');
        
        INSERT INTO COMMENTS (COMMENT_ID, RESULT_ID, USER_UUID, NICKNAME, MBTI_TYPE, CONTENT, LIKES_COUNT, IS_DELETED) VALUES
        (SEQ_COMMENT.NEXTVAL, NULL, 'guest-user-5', '분석하는ENTP', 'ENTP', 'MBTI 이론 자체가 흥미로워서 계속 파고들게 되네요. 여러분은 어떤 관점으로 MBTI를 보시나요?', 15, 'N');
    END IF;
END;
/

-- =============================================
-- 7단계: 트리거 생성 (자동 업데이트)
-- =============================================

-- 사용자 테이블 업데이트 트리거
CREATE OR REPLACE TRIGGER TRG_MBTI_USERS_UPDATE
    BEFORE UPDATE ON MBTI_USERS
    FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := CURRENT_TIMESTAMP;
END;
/

-- 댓글 테이블 업데이트 트리거
CREATE OR REPLACE TRIGGER TRG_COMMENTS_UPDATE
    BEFORE UPDATE ON COMMENTS
    FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := CURRENT_TIMESTAMP;
END;
/

-- =============================================
-- 8단계: 권한 및 동의어 설정 (필요시)
-- =============================================

-- 웹 애플리케이션 사용자에게 필요한 권한 부여 (예시)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON MBTI_USERS TO mbti_app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON COMMENTS TO mbti_app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON TEST_RESULTS TO mbti_app_user;

-- =============================================
-- 9단계: 최종 확인 및 정리
-- =============================================

-- 변경사항 커밋
COMMIT;

-- 생성된 테이블 구조 확인
PROMPT '=== 생성된 테이블들 ===';
SELECT 
    TABLE_NAME,
    TABLESPACE_NAME,
    STATUS
FROM USER_TABLES 
WHERE TABLE_NAME IN ('MBTI_USERS', 'COMMENTS', 'TEST_RESULTS')
ORDER BY TABLE_NAME;

PROMPT '';
PROMPT '=== COMMENTS 테이블 구조 ===';
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    NULLABLE,
    DATA_DEFAULT,
    COLUMN_ID
FROM USER_TAB_COLUMNS 
WHERE TABLE_NAME = 'COMMENTS'
ORDER BY COLUMN_ID;

PROMPT '';
PROMPT '=== 생성된 시퀀스들 ===';
SELECT 
    SEQUENCE_NAME,
    MIN_VALUE,
    MAX_VALUE,
    INCREMENT_BY,
    LAST_NUMBER
FROM USER_SEQUENCES 
WHERE SEQUENCE_NAME IN ('SEQ_MBTI_USER', 'SEQ_COMMENT', 'SEQ_TEST_RESULT')
ORDER BY SEQUENCE_NAME;

PROMPT '';
PROMPT '=== 생성된 인덱스들 ===';
SELECT 
    INDEX_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    COLUMN_POSITION
FROM USER_IND_COLUMNS 
WHERE TABLE_NAME IN ('MBTI_USERS', 'COMMENTS', 'TEST_RESULTS')
ORDER BY TABLE_NAME, INDEX_NAME, COLUMN_POSITION;

PROMPT '';
PROMPT '=== 샘플 데이터 확인 ===';
SELECT 'MBTI_USERS' as TABLE_NAME, COUNT(*) as ROW_COUNT FROM MBTI_USERS
UNION ALL
SELECT 'COMMENTS' as TABLE_NAME, COUNT(*) as ROW_COUNT FROM COMMENTS
UNION ALL  
SELECT 'TEST_RESULTS' as TABLE_NAME, COUNT(*) as ROW_COUNT FROM TEST_RESULTS
ORDER BY TABLE_NAME;

PROMPT '';
PROMPT '=== 커뮤니티 댓글 (RESULT_ID가 NULL인 댓글들) ===';
SELECT 
    COMMENT_ID,
    NICKNAME,
    MBTI_TYPE,
    SUBSTR(CONTENT, 1, 50) || '...' as CONTENT_PREVIEW,
    LIKES_COUNT,
    CREATED_AT
FROM COMMENTS 
WHERE RESULT_ID IS NULL
ORDER BY CREATED_AT DESC;

PROMPT '';
PROMPT '🎉 MBTI 커뮤니티 댓글 시스템 설정이 완료되었습니다!';
PROMPT '';
PROMPT '주요 변경사항:';
PROMPT '✅ MBTI_USERS 테이블 생성 (사용자 시스템)';
PROMPT '✅ COMMENTS.RESULT_ID를 NULL 허용으로 변경 (커뮤니티 댓글 지원)';
PROMPT '✅ USER_ID 컬럼 추가 (COMMENTS, TEST_RESULTS)';
PROMPT '✅ 필요한 시퀀스 및 인덱스 생성';
PROMPT '✅ 샘플 커뮤니티 댓글 데이터 추가';
PROMPT '✅ 자동 업데이트 트리거 설정';
PROMPT '';
PROMPT '이제 Spring Boot 애플리케이션을 재시작하고 커뮤니티 댓글 작성을 테스트해보세요!';
