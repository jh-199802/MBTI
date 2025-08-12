#!/bin/bash

# 네이버클라우드 배포 스크립트 (로컬 DB 연결)
echo "🚀 네이버클라우드 배포 시작 (로컬 Oracle DB 연결)"

# 설정값들 (실제 값으로 변경하세요)
CLOUD_SERVER_IP="YOUR_CLOUD_SERVER_IP"
SSH_KEY_PATH="your-key.pem"
SSH_USER="root"

# 환경변수 체크
if [ -z "$AI_KEY" ]; then
    echo "❌ AI_KEY 환경변수를 설정하세요:"
    echo "export AI_KEY='AIzaSyDzE3VKaNB67ersVExNXgEgko1C3MIBFK8'"
    exit 1
fi

if [ -z "$LOCAL_PUBLIC_IP" ]; then
    echo "❌ LOCAL_PUBLIC_IP 환경변수를 설정하세요:"
    echo "export LOCAL_PUBLIC_IP='your_public_ip'"
    echo "💡 공인 IP 확인: curl ifconfig.me"
    exit 1
fi

echo "✅ 환경변수 확인 완료"
echo "🏠 로컬 공인 IP: $LOCAL_PUBLIC_IP"
echo "☁️  클라우드 서버: $CLOUD_SERVER_IP"

# 1. 프로젝트 빌드
echo "📦 프로젝트 빌드 중..."
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "❌ 빌드 실패"
    exit 1
fi

# 2. JAR 파일 찾기
JAR_FILE=$(find build/libs -name "*.jar" | head -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "❌ JAR 파일을 찾을 수 없습니다."
    exit 1
fi

echo "📁 JAR 파일: $JAR_FILE"

# 3. application-cloud.properties 업데이트 (로컬 DB 연결)
echo "⚙️  클라우드 설정 업데이트 중..."
cat > temp-cloud.properties << EOF
# 네이버클라우드 배포용 설정 (로컬 Oracle DB 연결)
spring.profiles.active=cloud
server.port=8080

# 로컬 Oracle DB 연결 (공인 IP 사용)
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.url=jdbc:oracle:thin:@${LOCAL_PUBLIC_IP}:1521:XE
spring.datasource.username=C##JH
spring.datasource.password=1234

# JPA/Hibernate 설정
spring.jpa.hibernate.ddl-auto=none
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

# 커넥션 풀 설정
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=3
spring.datasource.hikari.connection-timeout=30000

# Thymeleaf 설정 (프로덕션용)
spring.thymeleaf.cache=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# API 키 설정
AI_KEY=${AI_KEY}

# 정적 리소스 설정
spring.web.resources.static-locations=classpath:/static/
spring.web.resources.cache.period=31536000
spring.web.resources.chain.strategy.content.enabled=true
spring.web.resources.chain.strategy.content.paths=/**

# 로깅 설정 (프로덕션용)
logging.level.com.example.mbtitest=INFO
logging.level.org.hibernate.SQL=WARN
logging.level.org.springframework=WARN
logging.level.root=INFO

# 보안 설정
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-stacktrace=never
server.error.include-exception=false

# 압축 설정
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
server.compression.min-response-size=1024
EOF

# 4. 클라우드 서버 환경 준비
echo "🌐 클라우드 서버 환경 준비 중..."
ssh -i "$SSH_KEY_PATH" "$SSH_USER@$CLOUD_SERVER_IP" << 'ENDSSH'
# Java 11 설치 확인
if ! java -version 2>&1 | grep -q "11"; then
    echo "📥 Java 11 설치 중..."
    apt update
    apt install -y openjdk-11-jdk
fi

# 기존 프로세스 중지
echo "🛑 기존 애플리케이션 중지..."
pkill -f "java.*jar" || true

# 애플리케이션 디렉토리 생성
mkdir -p ~/mbti-app
cd ~/mbti-app
ENDSSH

# 5. 파일 업로드
echo "📤 파일 업로드 중..."
scp -i "$SSH_KEY_PATH" "$JAR_FILE" "$SSH_USER@$CLOUD_SERVER_IP:~/mbti-app/"
scp -i "$SSH_KEY_PATH" temp-cloud.properties "$SSH_USER@$CLOUD_SERVER_IP:~/mbti-app/application-cloud.properties"

# 6. 애플리케이션 시작 스크립트 생성 및 업로드
cat > start-mbti.sh << 'EOF'
#!/bin/bash
echo "🚀 MBTI Test 서버 시작"

cd ~/mbti-app

# 환경변수 설정
export SPRING_PROFILES_ACTIVE=cloud
export SPRING_CONFIG_LOCATION=classpath:/application.properties,./application-cloud.properties

# 기존 프로세스 중지
pkill -f "java.*jar" || true
sleep 2

# 애플리케이션 시작
echo "🎯 애플리케이션 시작 중..."
nohup java -jar *.jar > app.log 2>&1 &

sleep 5

# 상태 확인
if pgrep -f "java.*jar" > /dev/null; then
    echo "✅ 서버가 성공적으로 시작되었습니다!"
    echo "🌐 앱 URL: http://$(curl -s ifconfig.me):8080"
    echo "📋 로그 확인: tail -f ~/mbti-app/app.log"
else
    echo "❌ 서버 시작 실패"
    echo "📋 로그 확인:"
    tail -10 app.log
    exit 1
fi
EOF

chmod +x start-mbti.sh
scp -i "$SSH_KEY_PATH" start-mbti.sh "$SSH_USER@$CLOUD_SERVER_IP:~/mbti-app/"

# 7. 서버에서 애플리케이션 시작
echo "🎯 서버에서 애플리케이션 시작 중..."
ssh -i "$SSH_KEY_PATH" "$SSH_USER@$CLOUD_SERVER_IP" << 'ENDSSH'
cd ~/mbti-app
chmod +x start-mbti.sh
./start-mbti.sh
ENDSSH

# 8. 임시 파일 정리
rm -f temp-cloud.properties start-mbti.sh

echo ""
echo "🎉 배포 완료!"
echo "💡 다음 단계:"
echo "1. 로컬 PC에서 Oracle DB 외부접속 허용 설정"
echo "2. 공유기 포트포워딩 1521 포트 설정"
echo "3. 방화벽에서 1521 포트 허용"
echo ""
echo "🔧 문제 발생시:"
echo "ssh -i $SSH_KEY_PATH $SSH_USER@$CLOUD_SERVER_IP 'tail -f ~/mbti-app/app.log'"
