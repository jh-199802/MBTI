-- =====================================================
-- MBTI 테스트 시스템 Oracle DB 스키마
-- 생성일: 2025-07-23
-- 작성자: Claude AI Assistant
-- =====================================================

-- 1. 시퀀스 생성
-- =====================================================

-- 테스트 결과 시퀀스
CREATE SEQUENCE SEQ_TEST_RESULT
    START WITH 1
    INCREMENT BY 1
    NOMAXVALUE
    NOCYCLE
    CACHE 20;

-- 댓글 시퀀스
CREATE SEQUENCE SEQ_COMMENT
    START WITH 1
    INCREMENT BY 1
    NOMAXVALUE
    NOCYCLE
    CACHE 20;

-- 공유 로그 시퀀스
CREATE SEQUENCE SEQ_SHARE_LOG
    START WITH 1
    INCREMENT BY 1
    NOMAXVALUE
    NOCYCLE
    CACHE 20;

-- 조회 로그 시퀀스
CREATE SEQUENCE SEQ_VIEW_LOG
    START WITH 1
    INCREMENT BY 1
    NOMAXVALUE
    NOCYCLE
    CACHE 20;

-- 2. 테이블 생성
-- =====================================================

-- 테스트 결과 테이블
CREATE TABLE TEST_RESULTS (
    RESULT_ID NUMBER(20) PRIMARY KEY,
    USER_UUID VARCHAR2(36) NOT NULL,
    USER_IP VARCHAR2(50),
    USER_AGENT VARCHAR2(500),
    MBTI_TYPE VARCHAR2(4) NOT NULL,
    MBTI_DESCRIPTION VARCHAR2(2000),
    MBTI_COLOR VARCHAR2(7),
    CATEGORY_SCORES CLOB,
    DETAILED_SCORES CLOB,
    ANSWER_DATA CLOB,
    AI_ANALYSIS CLOB,
    TEST_DURATION NUMBER(10),
    VIEW_COUNT NUMBER(10) DEFAULT 0,
    SHARED_COUNT NUMBER(10) DEFAULT 0,
    IS_PUBLIC CHAR(1) DEFAULT 'Y' CHECK (IS_PUBLIC IN ('Y', 'N')),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 댓글 테이블
CREATE TABLE COMMENTS (
    COMMENT_ID NUMBER(20) PRIMARY KEY,
    RESULT_ID NUMBER(20),
    MBTI_TYPE VARCHAR2(4) NOT NULL,
    NICKNAME VARCHAR2(50),
    COMMENT_TEXT VARCHAR2(1000) NOT NULL,
    USER_IP VARCHAR2(50),
    LIKES_COUNT NUMBER(10) DEFAULT 0,
    IS_DELETED CHAR(1) DEFAULT 'N' CHECK (IS_DELETED IN ('Y', 'N')),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 외래키 제약조건
    CONSTRAINT FK_COMMENT_RESULT FOREIGN KEY (RESULT_ID) REFERENCES TEST_RESULTS(RESULT_ID) ON DELETE CASCADE
);

-- 공유 로그 테이블
CREATE TABLE SHARE_LOGS (
    SHARE_ID NUMBER(20) PRIMARY KEY,
    RESULT_ID NUMBER(20),
    MBTI_TYPE VARCHAR2(4) NOT NULL,
    PLATFORM VARCHAR2(50) NOT NULL,
    USER_IP VARCHAR2(50),
    USER_AGENT VARCHAR2(500),
    REFERRER VARCHAR2(500),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 외래키 제약조건
    CONSTRAINT FK_SHARE_RESULT FOREIGN KEY (RESULT_ID) REFERENCES TEST_RESULTS(RESULT_ID) ON DELETE CASCADE
);

-- 조회 로그 테이블
CREATE TABLE VIEW_LOGS (
    VIEW_ID NUMBER(20) PRIMARY KEY,
    RESULT_ID NUMBER(20),
    MBTI_TYPE VARCHAR2(4) NOT NULL,
    USER_IP VARCHAR2(50),
    USER_AGENT VARCHAR2(500),
    REFERRER VARCHAR2(500),
    VIEW_DURATION NUMBER(10),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 외래키 제약조건
    CONSTRAINT FK_VIEW_RESULT FOREIGN KEY (RESULT_ID) REFERENCES TEST_RESULTS(RESULT_ID) ON DELETE CASCADE
);

-- 3. 인덱스 생성
-- =====================================================

-- 테스트 결과 테이블 인덱스
CREATE INDEX IDX_TEST_RESULTS_UUID ON TEST_RESULTS(USER_UUID);
CREATE INDEX IDX_TEST_RESULTS_MBTI ON TEST_RESULTS(MBTI_TYPE);
CREATE INDEX IDX_TEST_RESULTS_CREATED ON TEST_RESULTS(CREATED_AT);
CREATE INDEX IDX_TEST_RESULTS_PUBLIC ON TEST_RESULTS(IS_PUBLIC);
CREATE INDEX IDX_TEST_RESULTS_VIEW_COUNT ON TEST_RESULTS(VIEW_COUNT);
CREATE INDEX IDX_TEST_RESULTS_SHARED_COUNT ON TEST_RESULTS(SHARED_COUNT);

-- 댓글 테이블 인덱스
CREATE INDEX IDX_COMMENTS_RESULT_ID ON COMMENTS(RESULT_ID);
CREATE INDEX IDX_COMMENTS_MBTI ON COMMENTS(MBTI_TYPE);
CREATE INDEX IDX_COMMENTS_CREATED ON COMMENTS(CREATED_AT);
CREATE INDEX IDX_COMMENTS_LIKES ON COMMENTS(LIKES_COUNT);

-- 공유 로그 테이블 인덱스
CREATE INDEX IDX_SHARE_LOGS_RESULT_ID ON SHARE_LOGS(RESULT_ID);
CREATE INDEX IDX_SHARE_LOGS_MBTI ON SHARE_LOGS(MBTI_TYPE);
CREATE INDEX IDX_SHARE_LOGS_PLATFORM ON SHARE_LOGS(PLATFORM);
CREATE INDEX IDX_SHARE_LOGS_CREATED ON SHARE_LOGS(CREATED_AT);

-- 조회 로그 테이블 인덱스
CREATE INDEX IDX_VIEW_LOGS_RESULT_ID ON VIEW_LOGS(RESULT_ID);
CREATE INDEX IDX_VIEW_LOGS_MBTI ON VIEW_LOGS(MBTI_TYPE);
CREATE INDEX IDX_VIEW_LOGS_CREATED ON VIEW_LOGS(CREATED_AT);

-- 4. 트리거 생성 (업데이트 시간 자동 갱신)
-- =====================================================

-- 테스트 결과 업데이트 트리거
CREATE OR REPLACE TRIGGER TRG_TEST_RESULTS_UPDATE
    BEFORE UPDATE ON TEST_RESULTS
    FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := CURRENT_TIMESTAMP;
END;
/

-- 댓글 업데이트 트리거
CREATE OR REPLACE TRIGGER TRG_COMMENTS_UPDATE
    BEFORE UPDATE ON COMMENTS
    FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := CURRENT_TIMESTAMP;
END;
/

-- 5. 초기 데이터 삽입 (샘플 데이터)
-- =====================================================

-- MBTI 타입별 샘플 결과 데이터
INSERT INTO TEST_RESULTS (
    RESULT_ID, USER_UUID, USER_IP, MBTI_TYPE, MBTI_DESCRIPTION, MBTI_COLOR,
    CATEGORY_SCORES, DETAILED_SCORES, ANSWER_DATA, AI_ANALYSIS, TEST_DURATION,
    VIEW_COUNT, SHARED_COUNT, IS_PUBLIC
) VALUES (
    SEQ_TEST_RESULT.NEXTVAL,
    'sample-uuid-1',
    '192.168.1.1',
    'INTJ',
    '건축가 - 상상력이 풍부하고 전략적인 사고를 하는 사람',
    '#6C63FF',
    '{"E":35,"I":65,"S":40,"N":60,"T":70,"F":30,"J":75,"P":25}',
    '{"EI":{"E":35,"I":65,"tendency":"I"},"SN":{"S":40,"N":60,"tendency":"N"},"TF":{"T":70,"F":30,"tendency":"T"},"JP":{"J":75,"P":25,"tendency":"J"}}',
    '[1,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1]',
    'INTJ 타입으로 분석되었습니다. 당신은 독립적이고 결단력이 있으며, 미래 지향적인 사고를 가지고 있습니다.',
    125,
    150,
    5,
    'Y'
);

INSERT INTO TEST_RESULTS (
    RESULT_ID, USER_UUID, USER_IP, MBTI_TYPE, MBTI_DESCRIPTION, MBTI_COLOR,
    CATEGORY_SCORES, DETAILED_SCORES, ANSWER_DATA, AI_ANALYSIS, TEST_DURATION,
    VIEW_COUNT, SHARED_COUNT, IS_PUBLIC
) VALUES (
    SEQ_TEST_RESULT.NEXTVAL,
    'sample-uuid-2',
    '192.168.1.2',
    'ENFP',
    '활동가 - 열정적이고 창의적인 사회자',
    '#FD79A8',
    '{"E":75,"I":25,"S":30,"N":70,"T":40,"F":60,"J":20,"P":80}',
    '{"EI":{"E":75,"I":25,"tendency":"E"},"SN":{"S":30,"N":70,"tendency":"N"},"TF":{"T":40,"F":60,"tendency":"F"},"JP":{"J":20,"P":80,"tendency":"P"}}',
    '[3,1,3,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1]',
    'ENFP 타입으로 분석되었습니다. 당신은 열정적이고 창의적이며, 사람들과의 관계를 중시합니다.',
    98,
    89,
    12,
    'Y'
);

-- 샘플 댓글 데이터
INSERT INTO COMMENTS (
    COMMENT_ID, RESULT_ID, MBTI_TYPE, NICKNAME, COMMENT_TEXT, USER_IP, LIKES_COUNT
) VALUES (
    SEQ_COMMENT.NEXTVAL,
    1,
    'INTJ',
    'MBTI러버',
    '저도 INTJ인데 정말 정확한 분석이네요! 특히 미래 계획 세우는 부분이 완전 저랑 같아요.',
    '192.168.1.10',
    15
);

INSERT INTO COMMENTS (
    COMMENT_ID, RESULT_ID, MBTI_TYPE, NICKNAME, COMMENT_TEXT, USER_IP, LIKES_COUNT
) VALUES (
    SEQ_COMMENT.NEXTVAL,
    2,
    'ENFP',
    '카리스마있는사람',
    'ENFP 맞네요! 항상 새로운 아이디어가 막 떠올라서 정신없어요 ㅋㅋ',
    '192.168.1.11',
    8
);

-- 샘플 공유 로그 데이터
INSERT INTO SHARE_LOGS (
    SHARE_ID, RESULT_ID, MBTI_TYPE, PLATFORM, USER_IP
) VALUES (
    SEQ_SHARE_LOG.NEXTVAL,
    1,
    'INTJ',
    'kakao',
    '192.168.1.20'
);

INSERT INTO SHARE_LOGS (
    SHARE_ID, RESULT_ID, MBTI_TYPE, PLATFORM, USER_IP
) VALUES (
    SEQ_SHARE_LOG.NEXTVAL,
    1,
    'INTJ',
    'facebook',
    '192.168.1.21'
);

-- 샘플 조회 로그 데이터
INSERT INTO VIEW_LOGS (
    VIEW_ID, RESULT_ID, MBTI_TYPE, USER_IP, VIEW_DURATION
) VALUES (
    SEQ_VIEW_LOG.NEXTVAL,
    1,
    'INTJ',
    '192.168.1.30',
    45
);

INSERT INTO VIEW_LOGS (
    VIEW_ID, RESULT_ID, MBTI_TYPE, USER_IP, VIEW_DURATION
) VALUES (
    SEQ_VIEW_LOG.NEXTVAL,
    2,
    'ENFP',
    '192.168.1.31',
    67
);

-- 6. 통계 및 분석을 위한 뷰 생성
-- =====================================================

-- MBTI 타입별 통계 뷰
CREATE OR REPLACE VIEW V_MBTI_STATISTICS AS
SELECT 
    MBTI_TYPE,
    COUNT(*) AS TOTAL_COUNT,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM TEST_RESULTS), 2) AS PERCENTAGE,
    AVG(TEST_DURATION) AS AVG_DURATION,
    SUM(VIEW_COUNT) AS TOTAL_VIEWS,
    SUM(SHARED_COUNT) AS TOTAL_SHARES,
    AVG(VIEW_COUNT) AS AVG_VIEWS,
    AVG(SHARED_COUNT) AS AVG_SHARES
FROM TEST_RESULTS
WHERE IS_PUBLIC = 'Y'
GROUP BY MBTI_TYPE
ORDER BY TOTAL_COUNT DESC;

-- 일별 테스트 통계 뷰
CREATE OR REPLACE VIEW V_DAILY_TEST_STATS AS
SELECT 
    TRUNC(CREATED_AT) AS TEST_DATE,
    COUNT(*) AS DAILY_COUNT,
    COUNT(DISTINCT USER_UUID) AS UNIQUE_USERS,
    AVG(TEST_DURATION) AS AVG_DURATION,
    SUM(VIEW_COUNT) AS DAILY_VIEWS,
    SUM(SHARED_COUNT) AS DAILY_SHARES
FROM TEST_RESULTS
GROUP BY TRUNC(CREATED_AT)
ORDER BY TEST_DATE DESC;

-- 인기 결과 뷰 (조회수 + 공유수 + 댓글수 기준)
CREATE OR REPLACE VIEW V_POPULAR_RESULTS AS
SELECT 
    tr.RESULT_ID,
    tr.MBTI_TYPE,
    tr.VIEW_COUNT,
    tr.SHARED_COUNT,
    NVL(c.COMMENT_COUNT, 0) AS COMMENT_COUNT,
    (tr.VIEW_COUNT + tr.SHARED_COUNT * 5 + NVL(c.COMMENT_COUNT, 0) * 3) AS POPULARITY_SCORE,
    tr.CREATED_AT
FROM TEST_RESULTS tr
LEFT JOIN (
    SELECT RESULT_ID, COUNT(*) AS COMMENT_COUNT
    FROM COMMENTS 
    WHERE IS_DELETED = 'N'
    GROUP BY RESULT_ID
) c ON tr.RESULT_ID = c.RESULT_ID
WHERE tr.IS_PUBLIC = 'Y'
ORDER BY POPULARITY_SCORE DESC;

-- 7. 프로시저 생성 (성능 최적화를 위한)
-- =====================================================

-- 조회수 증가 프로시저
CREATE OR REPLACE PROCEDURE SP_INCREMENT_VIEW_COUNT(
    p_result_id IN NUMBER,
    p_user_ip IN VARCHAR2 DEFAULT NULL,
    p_user_agent IN VARCHAR2 DEFAULT NULL
) AS
BEGIN
    -- 조회수 증가
    UPDATE TEST_RESULTS 
    SET VIEW_COUNT = VIEW_COUNT + 1,
        UPDATED_AT = CURRENT_TIMESTAMP
    WHERE RESULT_ID = p_result_id;
    
    -- 조회 로그 기록
    INSERT INTO VIEW_LOGS (
        VIEW_ID, RESULT_ID, MBTI_TYPE, USER_IP, USER_AGENT
    )
    SELECT 
        SEQ_VIEW_LOG.NEXTVAL,
        p_result_id,
        MBTI_TYPE,
        p_user_ip,
        p_user_agent
    FROM TEST_RESULTS
    WHERE RESULT_ID = p_result_id;
    
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/

-- 공유수 증가 프로시저
CREATE OR REPLACE PROCEDURE SP_INCREMENT_SHARE_COUNT(
    p_result_id IN NUMBER,
    p_platform IN VARCHAR2,
    p_user_ip IN VARCHAR2 DEFAULT NULL,
    p_user_agent IN VARCHAR2 DEFAULT NULL
) AS
BEGIN
    -- 공유수 증가
    UPDATE TEST_RESULTS 
    SET SHARED_COUNT = SHARED_COUNT + 1,
        UPDATED_AT = CURRENT_TIMESTAMP
    WHERE RESULT_ID = p_result_id;
    
    -- 공유 로그 기록
    INSERT INTO SHARE_LOGS (
        SHARE_ID, RESULT_ID, MBTI_TYPE, PLATFORM, USER_IP, USER_AGENT
    )
    SELECT 
        SEQ_SHARE_LOG.NEXTVAL,
        p_result_id,
        MBTI_TYPE,
        p_platform,
        p_user_ip,
        p_user_agent
    FROM TEST_RESULTS
    WHERE RESULT_ID = p_result_id;
    
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/

-- 8. 권한 설정 및 동의어 생성 (필요시)
-- =====================================================

-- 웹 애플리케이션 사용자에게 권한 부여 (사용자명을 실제 환경에 맞게 변경)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON TEST_RESULTS TO mbti_app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON COMMENTS TO mbti_app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON SHARE_LOGS TO mbti_app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON VIEW_LOGS TO mbti_app_user;
-- GRANT SELECT ON V_MBTI_STATISTICS TO mbti_app_user;
-- GRANT SELECT ON V_DAILY_TEST_STATS TO mbti_app_user;
-- GRANT SELECT ON V_POPULAR_RESULTS TO mbti_app_user;
-- GRANT EXECUTE ON SP_INCREMENT_VIEW_COUNT TO mbti_app_user;
-- GRANT EXECUTE ON SP_INCREMENT_SHARE_COUNT TO mbti_app_user;

-- 9. 성능 모니터링을 위한 추가 쿼리들
-- =====================================================

-- 테이블 사이즈 체크 쿼리
/*
SELECT 
    table_name,
    num_rows,
    blocks,
    avg_row_len,
    ROUND(blocks * 8192 / 1024 / 1024, 2) AS size_mb
FROM user_tables 
WHERE table_name IN ('TEST_RESULTS', 'COMMENTS', 'SHARE_LOGS', 'VIEW_LOGS')
ORDER BY num_rows DESC;
*/

-- 인덱스 사용률 체크 쿼리
/*
SELECT 
    index_name,
    table_name,
    uniqueness,
    status,
    num_rows
FROM user_indexes 
WHERE table_name IN ('TEST_RESULTS', 'COMMENTS', 'SHARE_LOGS', 'VIEW_LOGS')
ORDER BY table_name, index_name;
*/

-- 10. 백업 및 아카이브 정책 (참고용)
-- =====================================================

-- 오래된 로그 데이터 아카이브 프로시저 (1년 이상 된 데이터)
/*
CREATE OR REPLACE PROCEDURE SP_ARCHIVE_OLD_LOGS AS
BEGIN
    -- 1년 이상 된 조회 로그 삭제
    DELETE FROM VIEW_LOGS 
    WHERE CREATED_AT < ADD_MONTHS(SYSDATE, -12);
    
    -- 1년 이상 된 공유 로그 삭제
    DELETE FROM SHARE_LOGS 
    WHERE CREATED_AT < ADD_MONTHS(SYSDATE, -12);
    
    COMMIT;
END;
/
*/

-- =====================================================
-- 스키마 생성 완료
-- =====================================================

COMMIT;

-- 생성된 객체 확인
SELECT 'Tables' AS object_type, table_name AS object_name FROM user_tables
WHERE table_name IN ('TEST_RESULTS', 'COMMENTS', 'SHARE_LOGS', 'VIEW_LOGS')
UNION ALL
SELECT 'Sequences' AS object_type, sequence_name AS object_name FROM user_sequences
WHERE sequence_name LIKE 'SEQ_%'
UNION ALL
SELECT 'Views' AS object_type, view_name AS object_name FROM user_views
WHERE view_name LIKE 'V_%'
UNION ALL
SELECT 'Procedures' AS object_type, object_name AS object_name FROM user_objects
WHERE object_type = 'PROCEDURE' AND object_name LIKE 'SP_%'
ORDER BY object_type, object_name;

-- 스키마 생성 성공 메시지
SELECT '🎉 MBTI 테스트 시스템 Oracle DB 스키마 생성이 완료되었습니다!' AS MESSAGE FROM DUAL;