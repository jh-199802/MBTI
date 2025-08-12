#!/bin/bash

# 서버에서 실행할 명령어들 (SSH 접속 후)

echo "🔧 네이버클라우드 서버에서 Oracle DB 연결 설정"

# 1. 현재 위치 확인
pwd
ls -la

# 2. Git 최신 버전 받기
echo "📥 최신 코드 받는 중..."
git pull origin main

# 3. 로컬 Oracle DB IP 설정 (실제 IP로 변경)
echo "🌐 로컬 Oracle DB IP 설정"
YOUR_LOCAL_PUBLIC_IP="$(curl -s ifconfig.me 로컬PC에서_확인한_IP)"  # 실제 로컬 공인 IP

# 4. application-cloud.properties 업데이트
echo "⚙️ Oracle DB 연결 설정 업데이트 중..."
sed -i "s/localhost/${YOUR_LOCAL_PUBLIC_IP}/g" src/main/resources/application-cloud.properties

# 5. Oracle JDBC 드라이버 확인 (build.gradle에 포함되어 있는지)
echo "🔍 Oracle JDBC 드라이버 확인..."
grep -i oracle build.gradle || echo "⚠️ Oracle 드라이버가 build.gradle에 없을 수 있습니다."

# 6. Java 11 설치 확인
echo "☕ Java 버전 확인..."
java -version
if ! java -version 2>&1 | grep -q "11"; then
    echo "📥 Java 11 설치 중..."
    sudo apt update
    sudo apt install -y openjdk-11-jdk
fi

# 7. 기존 애플리케이션 중지
echo "🛑 기존 애플리케이션 중지..."
pkill -f "java.*jar" || true

# 8. 프로젝트 빌드
echo "📦 Oracle DB 버전 빌드 중..."
chmod +x gradlew
./gradlew clean build -x test

# 9. 빌드 결과 확인
if [ $? -eq 0 ]; then
    echo "✅ 빌드 성공!"
    ls -la build/libs/
else
    echo "❌ 빌드 실패"
    echo "📋 가능한 원인:"
    echo "   1. Oracle JDBC 드라이버 의존성 문제"
    echo "   2. Java 버전 문제"
    echo "   3. 메모리 부족"
    exit 1
fi

# 10. Oracle DB 연결 테스트
echo "🔌 Oracle DB 연결 테스트 중..."
timeout 10 bash -c "</dev/tcp/${YOUR_LOCAL_PUBLIC_IP}/1521" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Oracle DB 포트 연결 가능"
else
    echo "❌ Oracle DB 연결 실패"
    echo "💡 확인 필요사항:"
    echo "   1. 로컬 PC Oracle DB 실행 중인지"
    echo "   2. 로컬 PC 포트포워딩 설정 (1521 포트)"
    echo "   3. 로컬 PC 방화벽 설정"
    echo "   4. 공인 IP 주소가 정확한지"
fi

# 11. 환경변수 설정 및 실행
echo "🚀 Oracle DB 버전 실행 준비..."
export SPRING_PROFILES_ACTIVE=cloud
export AI_KEY="AIzaSyDzE3VKaNB67ersVExNXgEgko1C3MIBFK8"

echo "📋 설정 완료:"
echo "   - Profile: $SPRING_PROFILES_ACTIVE"
echo "   - AI_KEY: ${AI_KEY:0:20}..."
echo "   - Oracle DB: ${YOUR_LOCAL_PUBLIC_IP}:1521"

echo ""
echo "🎯 이제 다음 명령어로 실행하세요:"
echo "   nohup java -jar build/libs/*.jar > app.log 2>&1 &"
echo ""
echo "📋 또는 기존 스크립트 수정 후 실행:"
echo "   ./start.sh  # Oracle DB 버전으로 수정된 경우"
