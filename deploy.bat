@echo off
REM MBTI Test 네이버클라우드 배포 스크립트 (Windows용)
REM 사용법: deploy.bat [app-engine|docker|vps]

setlocal enabledelayedexpansion

echo 🚀 MBTI Test 배포 스크립트 (Windows)
echo ==================================

REM 인수 체크
if "%1"=="" (
    echo 사용법: %0 [app-engine^|docker^|vps]
    pause
    exit /b 1
)

set DEPLOY_TYPE=%1

REM 환경변수 체크
:check_env_vars
if "%AI_KEY%"=="" (
    echo ❌ 오류: AI_KEY 환경변수가 설정되지 않았습니다.
    echo set AI_KEY=your_gemini_api_key 명령으로 설정하세요.
    pause
    exit /b 1
)
echo ✅ 환경변수 확인 완료

REM 프로젝트 빌드
:build_project
echo 📦 프로젝트 빌드 중...
call gradlew.bat clean build -x test

if %errorlevel% neq 0 (
    echo ❌ 빌드 실패
    pause
    exit /b 1
)
echo ✅ 빌드 성공

REM 배포 타입에 따른 분기
if "%DEPLOY_TYPE%"=="app-engine" goto deploy_app_engine
if "%DEPLOY_TYPE%"=="docker" goto deploy_docker
if "%DEPLOY_TYPE%"=="vps" goto deploy_vps

echo ❌ 잘못된 배포 타입: %DEPLOY_TYPE%
echo 사용 가능한 옵션: app-engine, docker, vps
pause
exit /b 1

REM App Engine 배포
:deploy_app_engine
echo 🌐 App Engine 배포 중...

REM app.yaml의 AI_KEY 값 업데이트 (PowerShell 사용)
powershell -Command "(Get-Content app.yaml) -replace '여기에_실제_Gemini_API_키_입력', '%AI_KEY%' | Set-Content app.yaml.tmp"
move app.yaml app.yaml.bak
move app.yaml.tmp app.yaml

REM 배포 실행
call gcloud app deploy --quiet

REM 백업 파일 복원
move app.yaml.bak app.yaml

echo ✅ App Engine 배포 완료
echo 🌐 앱 URL: https://your-project-id.appspot.com
goto end

REM Docker 배포
:deploy_docker
echo 🐳 Docker 배포 중...

REM Docker 이미지 빌드
docker build -t mbtitest:latest .

if %errorlevel% neq 0 (
    echo ❌ Docker 빌드 실패
    pause
    exit /b 1
)

REM 기존 컨테이너 중지 및 삭제
docker stop mbtitest 2>nul
docker rm mbtitest 2>nul

REM 새 컨테이너 실행
docker run -d --name mbtitest -p 8080:8080 -e AI_KEY="%AI_KEY%" -e SPRING_PROFILES_ACTIVE=prod --restart unless-stopped mbtitest:latest

if %errorlevel% neq 0 (
    echo ❌ Docker 실행 실패
    pause
    exit /b 1
)

echo ✅ Docker 배포 완료
echo 🌐 앱 URL: http://localhost:8080
goto end

REM VPS 배포
:deploy_vps
echo 🖥️ VPS용 JAR 파일 준비 중...

REM JAR 파일 찾기
for %%f in (build\libs\*.jar) do set JAR_FILE=%%f

if "%JAR_FILE%"=="" (
    echo ❌ JAR 파일을 찾을 수 없습니다.
    pause
    exit /b 1
)

echo ✅ JAR 파일 준비 완료: %JAR_FILE%
echo.
echo 📋 VPS 서버에서 실행할 명령어:
echo.
echo export AI_KEY='%AI_KEY%'
echo export SPRING_PROFILES_ACTIVE=prod
for %%f in ("%JAR_FILE%") do echo nohup java -jar %%~nxf ^> app.log 2^>^&1 ^&
echo.
echo 💡 파일 업로드 명령어:
for %%f in ("%JAR_FILE%") do echo scp -i your-key.pem %JAR_FILE% user@your-server:~/%%~nxf
goto end

:end
echo 🎉 배포 프로세스 완료!
pause
