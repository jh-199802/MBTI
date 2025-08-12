@echo off
REM MBTI 커뮤니티 댓글 문제 해결 스크립트
REM 사용법: fix_comments_issue.bat

echo 🔧 MBTI 커뮤니티 댓글 문제 해결 스크립트 시작
echo ================================================

REM 1. 데이터베이스 스키마 업데이트
echo 📄 데이터베이스 스키마 업데이트 중...
sqlplus C##JH/1234@localhost:1521/XE @src\main\resources\sql\setup_community_complete.sql

if %errorlevel% neq 0 (
    echo ❌ 데이터베이스 스키마 업데이트 실패
    pause
    exit /b 1
)

echo ✅ 데이터베이스 스키마 업데이트 완료

REM 2. Gradle 클린 빌드
echo 📦 애플리케이션 빌드 중...
call gradlew.bat clean build -x test

if %errorlevel% neq 0 (
    echo ❌ 애플리케이션 빌드 실패
    pause
    exit /b 1
)

echo ✅ 애플리케이션 빌드 완료

REM 3. 애플리케이션 실행
echo 🚀 애플리케이션 시작 중...
echo.
echo 📌 주요 변경사항:
echo   - Comment 엔티티 컬럼 매핑 수정 (CONTENT 컬럼 사용)
echo   - RESULT_ID NULL 허용으로 변경 (커뮤니티 댓글 지원)
echo   - 사용자 시스템 테이블 생성 (MBTI_USERS)
echo   - 샘플 커뮤니티 댓글 데이터 추가
echo.
echo 🌐 애플리케이션이 http://localhost:10000 에서 실행됩니다
echo 💬 커뮤니티 페이지: http://localhost:10000/comments
echo.
echo 애플리케이션 로그를 확인하여 댓글 작성이 정상 작동하는지 테스트해보세요!
echo.

REM 환경변수 설정 (필요시)
if "%AI_KEY%"=="" (
    echo ⚠️  경고: AI_KEY 환경변수가 설정되지 않았습니다.
    echo set AI_KEY=your_actual_api_key 로 설정하세요.
)

call gradlew.bat bootRun

echo.
echo 🎉 댓글 문제 해결 프로세스 완료!
pause
