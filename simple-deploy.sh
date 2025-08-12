#!/bin/bash

# MBTI Test 간단 배포 스크립트 (기존 DB 사용)
# 사용법: ./simple-deploy.sh [docker|vps]

echo "🚀 MBTI Test 간단 배포 (기존 DB 사용)"
echo "======================================"

DEPLOY_TYPE=$1

# 환경변수 체크
if [ -z "$AI_KEY" ]; then
    echo "❌ AI_KEY 환경변수를 설정하세요:"
    echo "export AI_KEY='AIzaSyDzE3VKaNB67ersVExNXgEgko1C3MIBFK8'"
    exit 1
fi

# 프로젝트 빌드
echo "📦 프로젝트 빌드 중..."
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "❌ 빌드 실패"
    exit 1
fi

case $DEPLOY_TYPE in
    "docker")
        echo "🐳 Docker 배포 중..."
        
        # 기존 컨테이너 중지 및 삭제
        docker stop mbtitest 2>/dev/null || true
        docker rm mbtitest 2>/dev/null || true
        
        # Docker 이미지 빌드
        docker build -t mbtitest:latest .
        
        # 컨테이너 실행 (호스트 네트워크 사용으로 localhost DB 접근)
        docker run -d \
            --name mbtitest \
            --network host \
            -e AI_KEY="$AI_KEY" \
            -e SPRING_PROFILES_ACTIVE=cloud \
            --restart unless-stopped \
            mbtitest:latest
        
        echo "✅ Docker 배포 완료!"
        echo "🌐 앱 URL: http://localhost:8080"
        echo "📋 로그 확인: docker logs mbtitest"
        ;;
        
    "vps")
        echo "🖥️ VPS용 패키지 준비 중..."
        
        JAR_FILE=$(find build/libs -name "*.jar" | head -n 1)
        if [ -z "$JAR_FILE" ]; then
            echo "❌ JAR 파일을 찾을 수 없습니다."
            exit 1
        fi
        
        # 배포 패키지 생성
        mkdir -p simple-deploy-package
        cp "$JAR_FILE" simple-deploy-package/
        
        # 시작 스크립트 생성
        cat > simple-deploy-package/start.sh << 'EOF'
#!/bin/bash
echo "🚀 MBTI Test 서버 시작"

if [ -z "$AI_KEY" ]; then
    echo "❌ AI_KEY 환경변수를 설정하세요:"
    echo "export AI_KEY='your_api_key'"
    exit 1
fi

export SPRING_PROFILES_ACTIVE=cloud
nohup java -jar *.jar > app.log 2>&1 &
echo "✅ 서버가 시작되었습니다."
echo "📋 로그 확인: tail -f app.log"
echo "🌐 앱 URL: http://localhost:8080"
EOF
        
        chmod +x simple-deploy-package/start.sh
        
        echo "✅ VPS 배포 패키지 준비 완료!"
        echo ""
        echo "📋 VPS 서버에서 실행:"
        echo "1. scp -r simple-deploy-package user@server:~/"
        echo "2. export AI_KEY='$AI_KEY'"
        echo "3. cd simple-deploy-package && ./start.sh"
        ;;
        
    *)
        echo "사용법: $0 [docker|vps]"
        exit 1
        ;;
esac

echo "🎉 배포 완료!"
