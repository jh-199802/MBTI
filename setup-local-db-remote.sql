-- =====================================================
-- 로컬 Oracle DB 외부접속 설정
-- 네이버클라우드에서 로컬 DB 연결을 위한 설정
-- =====================================================

-- 1. 리스너 설정 (모든 IP에서 접속 허용)
-- SQL*Plus에서 SYSDBA로 실행 필요
-- sqlplus / as sysdba

-- 현재 리스너 상태 확인
SHOW PARAMETER local_listener;

-- 리스너를 모든 IP에서 접속 가능하도록 설정
ALTER SYSTEM SET local_listener='(ADDRESS=(PROTOCOL=TCP)(HOST=0.0.0.0)(PORT=1521))' SCOPE=BOTH;

-- 리스너 등록
ALTER SYSTEM REGISTER;

-- 2. 네트워크 보안 설정 확인
-- $ORACLE_HOME/network/admin/sqlnet.ora 파일 확인
-- 다음 라인이 있다면 주석처리 또는 삭제:
-- SQLNET.AUTHENTICATION_SERVICES = (NTS)

-- 3. 사용자 권한 확인 (C##JH 계정)
-- 이미 생성된 계정이므로 권한만 확인
SELECT username, account_status FROM dba_users WHERE username = 'C##JH';

-- 필요시 계정 잠금 해제
-- ALTER USER C##JH ACCOUNT UNLOCK;

-- 4. 원격 접속 테스트용 뷰 생성
CREATE OR REPLACE VIEW V_CONNECTION_TEST AS
SELECT 
    'DB 연결 성공!' AS status,
    USER AS connected_user,
    SYSDATE AS connection_time,
    SYS_CONTEXT('USERENV', 'IP_ADDRESS') AS client_ip
FROM DUAL;

-- C##JH 사용자에게 뷰 권한 부여
GRANT SELECT ON V_CONNECTION_TEST TO C##JH;

-- 5. 방화벽 및 네트워크 설정 가이드
SELECT '=== 추가 설정 가이드 ===' AS guide FROM DUAL
UNION ALL
SELECT '1. Windows 방화벽에서 1521 포트 인바운드 허용' FROM DUAL
UNION ALL 
SELECT '2. 공유기 관리자 페이지에서 포트포워딩 설정:' FROM DUAL
UNION ALL
SELECT '   외부포트: 1521 → 내부IP:1521' FROM DUAL
UNION ALL
SELECT '3. 공인 IP 확인: https://www.whatismyip.com' FROM DUAL
UNION ALL
SELECT '4. 연결 테스트: telnet YOUR_PUBLIC_IP 1521' FROM DUAL;

-- 6. 연결 테스트 프로시저 생성
CREATE OR REPLACE PROCEDURE TEST_REMOTE_CONNECTION AS
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== Oracle DB 원격 접속 테스트 ===');
    DBMS_OUTPUT.PUT_LINE('현재 시간: ' || TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));
    DBMS_OUTPUT.PUT_LINE('접속 사용자: ' || USER);
    DBMS_OUTPUT.PUT_LINE('클라이언트 IP: ' || SYS_CONTEXT('USERENV', 'IP_ADDRESS'));
    DBMS_OUTPUT.PUT_LINE('데이터베이스: ' || SYS_CONTEXT('USERENV', 'DB_NAME'));
    DBMS_OUTPUT.PUT_LINE('=== 테스트 완료 ===');
END;
/

-- C##JH 사용자에게 프로시저 실행 권한 부여
GRANT EXECUTE ON TEST_REMOTE_CONNECTION TO C##JH;

-- 7. 테이블 존재 확인
SELECT 'TEST_RESULTS 테이블 존재 확인' AS check_type, COUNT(*) AS record_count 
FROM C##JH.TEST_RESULTS
UNION ALL
SELECT 'COMMENTS 테이블 존재 확인', COUNT(*) 
FROM C##JH.COMMENTS;

-- 8. 마지막 설정 확인 메시지
SELECT '🎉 Oracle DB 원격 접속 설정 완료!' AS message FROM DUAL
UNION ALL
SELECT '📋 다음 단계:' FROM DUAL  
UNION ALL
SELECT '1. 리스너 재시작: lsnrctl stop → lsnrctl start' FROM DUAL
UNION ALL
SELECT '2. 방화벽 설정 확인' FROM DUAL
UNION ALL
SELECT '3. 포트포워딩 설정 확인' FROM DUAL
UNION ALL
SELECT '4. 네이버클라우드에서 연결 테스트' FROM DUAL;

COMMIT;
