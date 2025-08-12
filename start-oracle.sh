#!/bin/bash

# Oracle DB 버전 시작 스크립트 (기존 start.sh 대체용)
echo "🚀 MBTI Test Oracle DB 버전 시작"

# 환경변수 확인
if [ -z "$AI_KEY" ]; then
    echo "🔑 AI_KEY 환경변수 설정 중..."
    export AI_KEY="AIzaSyDzE3VKaNB67ersVExNXgEgko1C3MIBFK8"
fi

# Oracle DB 연결을 위한 프로필 설정
export SPRING_PROFILES_ACTIVE=cloud

# 로컬 Oracle DB IP (실제 공인 IP로 변경 필요)
LOCAL_ORACLE_IP="YOUR_LOCAL_PUBLIC_IP"

echo "📋 설정 정보:"
echo "   - Profile: $SPRING_PROFILES_ACTIVE"
echo "   - AI_KEY: ${AI_KEY:0:20}..."
echo "   - Oracle DB: $LOCAL_ORACLE_IP:1521"

# 기존 프로세스 중지
echo "🛑 기존 프로세스 중지 중..."
pkill -f "java.*jar" || true
sleep 3

# JAR 파일 찾기
JAR_FILE=$(find build/libs -name "*.jar" | head -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "❌ JAR 파일을 찾을 수 없습니다."
    echo "💡 먼저 빌드를 실행하세요: ./gradlew build"
    exit 1
fi

echo "📁 실행 파일: $JAR_FILE"

# Oracle DB 연결 테스트
echo "🔌 Oracle DB 연결 테스트 중..."
timeout 5 bash -c "</dev/tcp/${LOCAL_ORACLE_IP}/1521" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Oracle DB 연결 확인"
else
    echo "⚠️  Oracle DB 연결 실패"
    echo "💡 확인사항:"
    echo "   1. 로컬 PC Oracle DB 실행 상태"
    echo "   2. 포트포워딩 설정 (1521 포트)"
    echo "   3. 방화벽 설정"
    echo ""
    echo "🤔 그래도 계속 진행하시겠습니까? (y/N)"
    read -r continue_anyway
    if [[ $continue_anyway != [yY] ]]; then
        echo "❌ 실행이 취소되었습니다."
        exit 0
    fi
fi

# 애플리케이션 시작
echo "🎯 Oracle DB 버전 애플리케이션 시작 중..."
nohup java -jar "$JAR_FILE" > app.log 2>&1 &

# 시작 확인
sleep 8
if pgrep -f "java.*jar" > /dev/null; then
    echo "✅ 서버가 성공적으로 시작되었습니다!"
    echo ""
    echo "🌐 웹사이트: http://$(curl -s ifconfig.me):8080"
    echo "🗄️  데이터베이스: Oracle DB (${LOCAL_ORACLE_IP}:1521/XE)"
    echo "👤 DB 사용자: C##JH"
    echo "📋 로그 확인: tail -f app.log"
    echo "🔄 프로세스 확인: ps aux | grep java"
    echo ""
    echo "🎉 MBTI 테스트 Oracle DB 버전이 실행 중입니다!"
    echo "   - 완전한 데이터베이스 기능"
    echo "   - 테스트 결과 영구 저장"
    echo "   - 댓글 및 커뮤니티 기능"
    echo "   - 통계 및 분석 기능"
else
    echo "❌ 서버 시작 실패"
    echo ""
    echo "📋 마지막 20줄 로그:"
    tail -20 app.log
    echo ""
    echo "🔍 일반적인 문제 해결:"
    echo "   1. Oracle DB 연결 확인"
    echo "   2. Java 힙 메모리 부족 → java -Xms512m -Xmx1024m -jar ..."
    echo "   3. 포트 충돌 확인 → netstat -tulpn | grep 8080"
    echo "   4. 전체 로그 확인 → cat app.log"
fi
