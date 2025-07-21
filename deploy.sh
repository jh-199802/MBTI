#!/bin/bash

# MBTI Test 네이버클라우드 배포 스크립트
# 사용법: ./deploy.sh [app-engine|docker|vps]

set -e  # 에러시 즉시 종료

# 컬러 출력 설정
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 MBTI Test 배포 스크립트${NC}"
echo "=================================="

# 인수 체크
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}사용법: $0 [app-engine|docker|vps]${NC}"
    exit 1
fi

DEPLOY_TYPE=$1

# 환경변수 체크
check_env_vars() {
    if [ -z "$AI_KEY" ]; then
        echo -e "${RED}❌ 오류: AI_KEY 환경변수가 설정되지 않았습니다.${NC}"
        echo "export AI_KEY='your_gemini_api_key' 명령으로 설정하세요."
        exit 1
    fi
    echo -e "${GREEN}✅ 환경변수 확인 완료${NC}"
}

# 프로젝트 빌드
build_project() {
    echo -e "${YELLOW}📦 프로젝트 빌드 중...${NC}"
    ./gradlew clean build -x test
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 빌드 성공${NC}"
    else
        echo -e "${RED}❌ 빌드 실패${NC}"
        exit 1
    fi
}

# App Engine 배포
deploy_app_engine() {
    echo -e "${YELLOW}🌐 App Engine 배포 중...${NC}"
    
    # app.yaml의 AI_KEY 값 업데이트
    sed -i.bak "s/여기에_실제_Gemini_API_키_입력/$AI_KEY/g" app.yaml
    
    # 배포 실행
    gcloud app deploy --quiet
    
    # 백업 파일 복원
    mv app.yaml.bak app.yaml
    
    echo -e "${GREEN}✅ App Engine 배포 완료${NC}"
    echo -e "${GREEN}🌐 앱 URL: https://your-project-id.appspot.com${NC}"
}

# Docker 배포
deploy_docker() {
    echo -e "${YELLOW}🐳 Docker 배포 중...${NC}"
    
    # Docker 이미지 빌드
    docker build -t mbtitest:latest .
    
    # 컨테이너 실행 (기존 컨테이너가 있으면 중지 후 삭제)
    docker stop mbtitest 2>/dev/null || true
    docker rm mbtitest 2>/dev/null || true
    
    docker run -d \
        --name mbtitest \
        -p 8080:8080 \
        -e AI_KEY="$AI_KEY" \
        -e SPRING_PROFILES_ACTIVE=prod \
        --restart unless-stopped \
        mbtitest:latest
    
    echo -e "${GREEN}✅ Docker 배포 완료${NC}"
    echo -e "${GREEN}🌐 앱 URL: http://localhost:8080${NC}"
}

# VPS 배포 (JAR 파일 생성)
deploy_vps() {
    echo -e "${YELLOW}🖥️  VPS용 JAR 파일 준비 중...${NC}"
    
    JAR_FILE=$(find build/libs -name "*.jar" | head -n 1)
    if [ -z "$JAR_FILE" ]; then
        echo -e "${RED}❌ JAR 파일을 찾을 수 없습니다.${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ JAR 파일 준비 완료: $JAR_FILE${NC}"
    echo -e "${YELLOW}📋 VPS 서버에서 실행할 명령어:${NC}"
    echo ""
    echo "export AI_KEY='$AI_KEY'"
    echo "export SPRING_PROFILES_ACTIVE=prod"
    echo "nohup java -jar $(basename $JAR_FILE) > app.log 2>&1 &"
    echo ""
    echo -e "${YELLOW}💡 파일 업로드 명령어:${NC}"
    echo "scp -i your-key.pem $JAR_FILE user@your-server:~/"
}

# 메인 실행 로직
main() {
    check_env_vars
    build_project
    
    case $DEPLOY_TYPE in
        "app-engine")
            deploy_app_engine
            ;;
        "docker")
            deploy_docker
            ;;
        "vps")
            deploy_vps
            ;;
        *)
            echo -e "${RED}❌ 잘못된 배포 타입: $DEPLOY_TYPE${NC}"
            echo -e "${YELLOW}사용 가능한 옵션: app-engine, docker, vps${NC}"
            exit 1
            ;;
    esac
}

# 스크립트 실행
main

echo -e "${GREEN}🎉 배포 프로세스 완료!${NC}"
