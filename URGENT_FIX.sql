-- 즉시 실행할 댓글 문제 해결 SQL
-- Oracle DB에 접속해서 이 스크립트를 실행하세요

-- 1. COMMENTS 테이블의 RESULT_ID를 NULL 허용으로 변경 (커뮤니티 댓글용)
ALTER TABLE COMMENTS MODIFY RESULT_ID NUMBER NULL;

-- 2. 테스트용 샘플 데이터 하나 추가 (댓글 작성 테스트용)
INSERT INTO TEST_RESULTS (
    RESULT_ID, USER_UUID, MBTI_TYPE, DETAILED_SCORES, AI_ANALYSIS, TEST_ANSWERS
) VALUES (
    1,
    'sample-uuid-test',
    'ENFP',
    '{"E":70,"I":30,"S":40,"N":60,"T":35,"F":65,"J":25,"P":75}',
    'ENFP 타입으로 분석되었습니다.',
    '[1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1]'
);

-- 3. 현재 테이블 구조 확인
DESC COMMENTS;

-- 4. 샘플 댓글 추가해서 테스트
INSERT INTO COMMENTS (
    COMMENT_ID, RESULT_ID, USER_UUID, NICKNAME, MBTI_TYPE, CONTENT, LIKES_COUNT, IS_DELETED
) VALUES (
    1, NULL, 'test-user', '테스터', 'ENFP', '테스트 댓글입니다!', 0, 'N'
);

COMMIT;

SELECT '✅ 댓글 시스템 수정 완료!' as STATUS FROM DUAL;
