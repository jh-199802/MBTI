#!/bin/bash

# Oracle DB 버전 업데이트 스크립트
echo "🔄 Oracle DB 버전으로 업데이트 시작"

# 기존 애플리케이션 중지
echo "🛑 기존 애플리케이션 중지 중..."
pkill -f "java.*jar" || true
sleep 3

# 현재 Git 상태 확인
echo "📋 현재 Git 상태:"
git status
git log --oneline -5

echo ""
echo "🔄 Oracle DB 버전으로 업데이트하시겠습니까? (y/N)"
read -r confirm

if [[ $confirm != [yY] ]]; then
    echo "❌ 업데이트가 취소되었습니다."
    exit 0
fi

# Git 변경사항 백업
echo "💾 현재 변경사항 백업 중..."
git stash push -m "backup_before_oracle_update_$(date +%Y%m%d_%H%M%S)"

# 최신 Oracle DB 버전 Pull
echo "📥 최신 Oracle DB 버전 가져오는 중..."
git pull origin main

# Oracle DB 연결 설정 확인
echo "🔍 Oracle DB 연결 설정 확인..."
if [ ! -f "src/main/resources/application-cloud.properties" ]; then
    echo "❌ application-cloud.properties 파일이 없습니다."
    echo "💡 Git에 최신 버전이 푸시되었는지 확인하세요."
    exit 1
fi

# 로컬 Oracle DB IP 설정
echo "🌐 로컬 Oracle DB 연결 설정 중..."
LOCAL_ORACLE_IP="YOUR_LOCAL_PUBLIC_IP"  # 실제 공인 IP로 변경 필요

# application-cloud.properties 업데이트
cat > temp-oracle-config.properties << EOF
# 네이버클라우드에서 로컬 Oracle DB 연결
spring.profiles.active=cloud
server.port=8080

# 로컬 Oracle DB 연결 설정
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.url=jdbc:oracle:thin:@${LOCAL_ORACLE_IP}:1521:XE
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
spring.datasource.hikari.connection-test-query=SELECT 1 FROM DUAL

# AI API 키 (기존 환경변수 사용)
AI_KEY=\${AI_KEY:AIzaSyDzE3VKaNB67ersVExNXgEgko1C3MIBFK8}

# Thymeleaf 설정
spring.thymeleaf.cache=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# 로깅 설정
logging.level.com.example.mbtitest=INFO
logging.level.org.hibernate.SQL=WARN
logging.level.org.springframework=WARN
logging.level.root=INFO

# 보안 설정
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-stacktrace=never

# 압축 설정
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
EOF

# 설정 파일 교체
cp temp-oracle-config.properties src/main/resources/application-cloud.properties
rm temp-oracle-config.properties

# 프로젝트 빌드
echo "📦 Oracle DB 버전 빌드 중..."
chmod +x gradlew
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "❌ 빌드 실패"
    echo "🔄 이전 버전으로 롤백 중..."
    git stash pop
    exit 1
fi

# JAR 파일 확인
JAR_FILE=$(find build/libs -name "*.jar" | head -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "❌ JAR 파일을 찾을 수 없습니다."
    exit 1
fi

echo "✅ 빌드 성공: $JAR_FILE"

# 환경변수 설정
export SPRING_PROFILES_ACTIVE=cloud
export AI_KEY="AIzaSyDzE3VKaNB67ersVExNXgEgko1C3MIBFK8"

# Oracle DB 연결 테스트
echo "🔌 Oracle DB 연결 테스트 중..."
timeout 10 bash -c "</dev/tcp/${LOCAL_ORACLE_IP}/1521" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Oracle DB 연결 가능"
else
    echo "⚠️  Oracle DB 연결 실패 - 포트포워딩 확인 필요"
    echo "💡 계속 진행하려면 Enter, 중단하려면 Ctrl+C"
    read
fi

# 애플리케이션 시작
echo "🚀 Oracle DB 버전 애플리케이션 시작 중..."
nohup java -jar "$JAR_FILE" > app.log 2>&1 &

sleep 8

# 애플리케이션 상태 확인
if pgrep -f "java.*jar" > /dev/null; then
    echo "✅ Oracle DB 버전 서버가 성공적으로 시작되었습니다!"
    echo "🌐 앱 URL: http://$(curl -s ifconfig.me):8080"
    echo "🗄️  데이터베이스: 로컬 Oracle DB (${LOCAL_ORACLE_IP}:1521)"
    echo "📋 로그 확인: tail -f app.log"
    echo ""
    echo "🎯 MBTI 테스트가 이제 완전한 DB 기능과 함께 실행됩니다:"
    echo "   - 테스트 결과 저장"
    echo "   - 댓글 시스템"
    echo "   - 조회수/공유수 통계"
    echo "   - 커뮤니티 기능"
else
    echo "❌ 서버 시작 실패"
    echo "📋 로그 확인:"
    tail -20 app.log
    echo ""
    echo "🔄 이전 버전으로 롤백하시겠습니까? (y/N)"
    read -r rollback
    if [[ $rollback == [yY] ]]; then
        git stash pop
        echo "✅ 이전 버전으로 롤백 완료"
    fi
fi
