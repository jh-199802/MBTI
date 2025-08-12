@echo off
REM Oracle DB 원격 접속을 위한 Windows 방화벽 설정
REM 관리자 권한으로 실행 필요

echo 🔥 Oracle DB 원격 접속용 방화벽 설정
echo =====================================

REM 관리자 권한 확인
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo ❌ 관리자 권한이 필요합니다.
    echo 우클릭 → "관리자 권한으로 실행"을 선택하세요.
    pause
    exit /b 1
)

echo ✅ 관리자 권한 확인됨

REM Oracle DB 포트 1521 인바운드 규칙 추가
echo 📥 1521 포트 인바운드 규칙 추가 중...
netsh advfirewall firewall add rule name="Oracle DB 1521 Inbound" dir=in action=allow protocol=TCP localport=1521
if %errorlevel% equ 0 (
    echo ✅ 인바운드 규칙 추가 성공
) else (
    echo ❌ 인바운드 규칙 추가 실패
)

REM Oracle DB 포트 1521 아웃바운드 규칙 추가
echo 📤 1521 포트 아웃바운드 규칙 추가 중...
netsh advfirewall firewall add rule name="Oracle DB 1521 Outbound" dir=out action=allow protocol=TCP localport=1521
if %errorlevel% equ 0 (
    echo ✅ 아웃바운드 규칙 추가 성공
) else (
    echo ❌ 아웃바운드 규칙 추가 실패
)

REM 현재 방화벽 규칙 확인
echo 🔍 추가된 방화벽 규칙 확인:
netsh advfirewall firewall show rule name="Oracle DB 1521 Inbound"
netsh advfirewall firewall show rule name="Oracle DB 1521 Outbound"

echo.
echo 🎉 방화벽 설정 완료!
echo.
echo 📋 다음 단계:
echo 1. 공유기 관리자 페이지 접속 (보통 192.168.1.1 또는 192.168.0.1)
echo 2. 포트포워딩 설정:
echo    - 외부포트: 1521
echo    - 내부IP: 현재PC IP
echo    - 내부포트: 1521
echo 3. Oracle 리스너 재시작
echo 4. 공인 IP 확인 및 테스트

pause
