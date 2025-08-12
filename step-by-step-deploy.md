# 🚀 단계별 배포 가이드 (Method 1)

## ✅ **사전 준비 체크리스트**

### 1. 네이버클라우드 서버 정보 확인
- [ ] 서버 공인 IP: `_______________`
- [ ] SSH 키 파일: `mbti-server-key.pem` 
- [ ] SSH 사용자: `root` 또는 `ubuntu`

### 2. 로컬 PC 정보 확인
- [ ] 공인 IP 확인: `curl ifconfig.me` 결과 = `_______________`
- [ ] Oracle DB 실행 중 확인
- [ ] AI_KEY 확인: `AIzaSyDzE3VKaNB67ersVExNXgEgko1C3MIBFK8`

## 🔥 **1단계: 로컬 Oracle DB 외부접속 설정**

### A. Oracle DB 설정 (5분)
```bash
# SQL*Plus 실행 (관리자 권한)
sqlplus / as sysdba

# 스크립트 실행
@setup-local-db-remote.sql
```

### B. Windows 방화벽 설정 (2분)
```bash
# 관리자 권한으로 실행
setup-windows-firewall.bat
```

### C. 공유기 포트포워딩 설정 (3분)
1. 브라우저에서 `192.168.1.1` 또는 `192.168.0.1` 접속
2. 관리자 로그인
3. 고급설정 → 포트포워딩/NAT/가상서버
4. 새 규칙 추가:
   - 서비스명: Oracle DB
   - 외부포트: 1521
   - 내부IP: 현재 PC IP (예: 192.168.1.100)
   - 내부포트: 1521
   - 프로토콜: TCP

## 🚀 **2단계: 배포 스크립트 설정 및 실행**

### A. 배포 스크립트 수정 (2분)
```bash
# deploy-to-naver-cloud.sh 파일 열기
# 다음 값들을 실제 정보로 변경:

CLOUD_SERVER_IP="실제_서버_공인_IP"     # 네이버클라우드 서버 IP
SSH_KEY_PATH="실제_키_파일_경로"        # 예: ./mbti-server-key.pem
SSH_USER="root"                       # 또는 ubuntu
```

### B. 환경변수 설정 (1분)
```bash
# Git Bash 또는 PowerShell에서 실행
export AI_KEY="AIzaSyDzE3VKaNB67ersVExNXgEgko1C3MIBFK8"
export LOCAL_PUBLIC_IP="$(curl -s ifconfig.me)"

# 확인
echo "AI_KEY: $AI_KEY"
echo "LOCAL_PUBLIC_IP: $LOCAL_PUBLIC_IP"
```

### C. 배포 스크립트 실행 (5분)
```bash
# 실행 권한 부여
chmod +x deploy-to-naver-cloud.sh

# 배포 실행
./deploy-to-naver-cloud.sh
```

## 🔍 **3단계: 배포 결과 확인**

### A. 연결 테스트
```bash
# 1. Oracle DB 포트 테스트
telnet [로컬_공인_IP] 1521

# 2. 웹사이트 접속 테스트
# 브라우저에서 http://[클라우드_서버_IP]:8080 접속
```

### B. 로그 확인 (문제 발생시)
```bash
# 클라우드 서버 접속
ssh -i mbti-server-key.pem root@클라우드서버IP

# 애플리케이션 로그 확인
tail -f ~/mbti-app/app.log
```

## 🎉 **성공 확인 방법**

배포가 성공하면 다음과 같이 표시됩니다:
```
✅ 서버가 성공적으로 시작되었습니다!
🌐 앱 URL: http://123.456.789.123:8080
📋 로그 확인: tail -f ~/mbti-app/app.log
```

웹브라우저에서 `http://클라우드서버IP:8080`에 접속하여 MBTI 테스트가 정상 작동하는지 확인!

## ❌ **문제 해결**

### 자주 발생하는 문제들:

1. **DB 연결 실패**
   - 포트포워딩 설정 확인
   - 방화벽 설정 확인
   - Oracle DB 리스너 상태 확인

2. **SSH 접속 실패**
   - 키 파일 권한 확인: `chmod 400 mbti-server-key.pem`
   - 서버 IP 주소 확인
   - 네이버클라우드 ACG 설정 확인

3. **빌드 실패**
   - Java 11 설치 확인
   - Gradle wrapper 권한 확인: `chmod +x gradlew`

도움이 필요하면 언제든 말씀하세요! 🙋‍♂️
