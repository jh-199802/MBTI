-- 더미 테스트 결과 삽입 (댓글 작성을 위한 임시 데이터)
-- 이 스크립트를 실행하여 최소 하나의 테스트 결과를 만들어둡니다.

-- 1. 더미 테스트 결과 삽입
INSERT INTO TEST_RESULTS (
    RESULT_ID,
    USER_IP,
    USER_AGENT,
    MBTI_TYPE,
    CATEGORY_SCORES,
    ANSWER_DATA,
    AI_ANALYSIS,
    TEST_DURATION,
    CREATED_AT,
    UPDATED_AT
) VALUES (
    1,  -- RESULT_ID
    '127.0.0.1',  -- USER_IP
    'Dummy User Agent',  -- USER_AGENT
    'ENFP',  -- MBTI_TYPE
    '{"E":60,"I":40,"S":45,"N":55,"T":30,"F":70,"J":25,"P":75}',  -- CATEGORY_SCORES
    '[1,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1,3,2,1]',  -- ANSWER_DATA
    'ENFP 타입으로 분석되었습니다. 열정적이고 창의적인 성격입니다.',  -- AI_ANALYSIS
    120,  -- TEST_DURATION
    CURRENT_TIMESTAMP,  -- CREATED_AT
    CURRENT_TIMESTAMP   -- UPDATED_AT
);

-- 2. 더미 데이터가 이미 존재하는지 확인하고 없으면 삽입
MERGE INTO TEST_RESULTS tr
USING (SELECT 1 as result_id FROM dual) dummy
ON (tr.RESULT_ID = dummy.result_id)
WHEN NOT MATCHED THEN
    INSERT (RESULT_ID, USER_IP, MBTI_TYPE, CATEGORY_SCORES, ANSWER_DATA, AI_ANALYSIS, TEST_DURATION)
    VALUES (1, '127.0.0.1', 'ENFP', '{"E":60,"I":40,"S":45,"N":55,"T":30,"F":70,"J":25,"P":75}', 
            '[1,2,1,3]', 'ENFP 더미 분석', 120);

COMMIT;

-- 3. 삽입 결과 확인
SELECT RESULT_ID, MBTI_TYPE, CREATED_AT FROM TEST_RESULTS WHERE RESULT_ID = 1;
