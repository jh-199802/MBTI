#!/bin/bash

# MBTI Test 네이버클라우드 배포 스크립트 (DB 초기화 포함)
# 사용법: ./deploy.sh [app-engine|docker|vps]

set -e  # 에러시 즉시 종료

# 컬러 출력 설정
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 MBTI Test 배포 스크립트 (DB 초기화 포함)${NC}"
echo "================================================="

# 인수 체크
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}사용법: $0 [app-engine|docker|vps]${NC}"
    echo -e "${YELLOW}추가 옵션: --skip-db (DB 초기화 건너뛰기)${NC}"
    exit 1
fi

DEPLOY_TYPE=$1
SKIP_DB=false

# 옵션 체크
if [[ "$2" == "--skip-db" ]]; then
    SKIP_DB=true
    echo -e "${YELLOW}⚠️  DB 초기화를 건너뜁니다.${NC}"
fi

# 환경변수 체크
check_env_vars() {
    echo -e "${YELLOW}🔍 환경변수 확인 중...${NC}"
    
    if [ -z "$AI_KEY" ]; then
        echo -e "${RED}❌ 오류: AI_KEY 환경변수가 설정되지 않았습니다.${NC}"
        echo "export AI_KEY='your_gemini_api_key' 명령으로 설정하세요."
        exit 1
    fi
    
    # 클라우드 배포시 DB 환경변수 체크
    if [[ "$DEPLOY_TYPE" != "docker" && "$SKIP_DB" == "false" ]]; then
        if [ -z "$DB_URL" ] || [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ]; then
            echo -e "${RED}❌ 오류: 클라우드 DB 환경변수가 설정되지 않았습니다.${NC}"
            echo "다음 환경변수를 설정하세요:"
            echo "export DB_URL='jdbc:oracle:thin:@클라우드DB주소:1521:XE'"
            echo "export DB_USERNAME='클라우드사용자명'"
            echo "export DB_PASSWORD='클라우드비밀번호'"
            exit 1
        fi
    fi
    
    echo -e "${GREEN}✅ 환경변수 확인 완료${NC}"
}

# 데이터베이스 초기화
init_database() {
    if [ "$SKIP_DB" == "true" ]; then
        echo -e "${YELLOW}⏭️  DB 초기화를 건너뜁니다.${NC}"
        return
    fi
    
    echo -e "${YELLOW}🗄️  데이터베이스 초기화 중...${NC}"
    
    # Oracle 클라이언트 설치 확인
    if ! command -v sqlplus &> /dev/null; then
        echo -e "${RED}❌ Oracle SQLPlus가 설치되지 않았습니다.${NC}"
        echo -e "${YELLOW}💡 다음 중 하나를 선택하세요:${NC}"
        echo "1. Oracle Instant Client 설치"
        echo "2. --skip-db 옵션으로 배포 후 수동 DB 설정"
        echo "3. Docker 배포 방식 사용 (DB 자동 초기화 포함)"
        exit 1
    fi
    
    # DB 연결 정보 추출
    DB_HOST=$(echo $DB_URL | sed -n 's/.*@\([^:]*\):.*/\1/p')
    DB_PORT=$(echo $DB_URL | sed -n 's/.*:\([0-9]*\)\/.*/\1/p')
    DB_SID=$(echo $DB_URL | sed -n 's/.*\/\(.*\)/\1/p')
    
    # 스키마 파일 존재 확인
    SCHEMA_FILE="database/oracle_schema.sql"
    if [ ! -f "$SCHEMA_FILE" ]; then
        SCHEMA_FILE="oracle_schema.sql"
        if [ ! -f "$SCHEMA_FILE" ]; then
            echo -e "${RED}❌ 스키마 파일을 찾을 수 없습니다.${NC}"
            exit 1
        fi
    fi
    
    echo -e "${YELLOW}📋 DB 연결 정보:${NC}"
    echo "호스트: $DB_HOST"
    echo "포트: $DB_PORT"
    echo "SID: $DB_SID"
    echo "사용자: $DB_USERNAME"
    echo ""
    
    # DB 연결 테스트
    echo -e "${YELLOW}🔌 DB 연결 테스트 중...${NC}"
    echo "SELECT 'DB 연결 성공!' FROM DUAL;" | sqlplus -S "$DB_USERNAME/$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_SID" > /dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ DB 연결 성공${NC}"
    else
        echo -e "${RED}❌ DB 연결 실패${NC}"
        echo "DB 연결 정보를 확인하세요."
        exit 1
    fi
    
    # 스키마 실행
    echo -e "${YELLOW}📄 스키마 실행 중...${NC}"
    sqlplus -S "$DB_USERNAME/$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_SID" @"$SCHEMA_FILE"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 데이터베이스 초기화 완료${NC}"
    else
        echo -e "${RED}❌ 스키마 실행 실패${NC}"
        echo "수동으로 스키마를 실행하거나 --skip-db 옵션을 사용하세요."
        exit 1
    fi
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
    
    # app.yaml의 환경변수 업데이트
    cp app.yaml app.yaml.bak
    sed -i "s/여기에_실제_Gemini_API_키_입력/$AI_KEY/g" app.yaml
    
    # 배포 실행
    gcloud app deploy --quiet
    
    # 백업 파일 복원
    mv app.yaml.bak app.yaml
    
    echo -e "${GREEN}✅ App Engine 배포 완료${NC}"
    echo -e "${GREEN}🌐 앱 URL: https://your-project-id.appspot.com${NC}"
}

# Docker 배포 (DB 초기화 포함)
deploy_docker() {
    echo -e "${YELLOW}🐳 Docker 배포 중...${NC}"
    
    # Dockerfile에 DB 초기화 스크립트 추가
    cat > Dockerfile.complete << 'EOF'
FROM openjdk:11-jre-slim

# Oracle Instant Client 설치 (DB 초기화용)
RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget https://download.oracle.com/otn_software/linux/instantclient/instantclient-basiclite-linuxx64.zip && \
    unzip instantclient-basiclite-linuxx64.zip && \
    mv instantclient_* /opt/oracle && \
    echo '/opt/oracle' > /etc/ld.so.conf.d/oracle.conf && \
    ldconfig && \
    apt-get clean

# 환경변수 설정
ENV PATH="/opt/oracle:${PATH}"
ENV LD_LIBRARY_PATH="/opt/oracle"

# 애플리케이션 파일 복사
COPY build/libs/*.jar app.jar
COPY database/oracle_schema.sql /opt/schema.sql

# 시작 스크립트 생성
RUN echo '#!/bin/bash' > /opt/start.sh && \
    echo 'if [ "$INIT_DB" = "true" ]; then' >> /opt/start.sh && \
    echo '  echo "DB 초기화 중..."' >> /opt/start.sh && \
    echo '  sqlplus -S $DB_USERNAME/$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_SID @/opt/schema.sql' >> /opt/start.sh && \
    echo 'fi' >> /opt/start.sh && \
    echo 'java -jar app.jar' >> /opt/start.sh && \
    chmod +x /opt/start.sh

EXPOSE 8080
CMD ["/opt/start.sh"]
EOF

    # Docker 이미지 빌드
    docker build -f Dockerfile.complete -t mbtitest:latest .
    
    # 기존 컨테이너 중지 및 삭제
    docker stop mbtitest 2>/dev/null || true
    docker rm mbtitest 2>/dev/null || true
    
    # 새 컨테이너 실행
    docker run -d \
        --name mbtitest \
        -p 8080:8080 \
        -e AI_KEY="$AI_KEY" \
        -e SPRING_PROFILES_ACTIVE=cloud \
        -e DB_URL="$DB_URL" \
        -e DB_USERNAME="$DB_USERNAME" \
        -e DB_PASSWORD="$DB_PASSWORD" \
        -e INIT_DB="true" \
        --restart unless-stopped \
        mbtitest:latest
    
    # 임시 파일 정리
    rm -f Dockerfile.complete
    
    echo -e "${GREEN}✅ Docker 배포 완료${NC}"
    echo -e "${GREEN}🌐 앱 URL: http://localhost:8080${NC}"
}

# VPS 배포
deploy_vps() {
    echo -e "${YELLOW}🖥️  VPS용 배포 패키지 준비 중...${NC}"
    
    JAR_FILE=$(find build/libs -name "*.jar" | head -n 1)
    if [ -z "$JAR_FILE" ]; then
        echo -e "${RED}❌ JAR 파일을 찾을 수 없습니다.${NC}"
        exit 1
    fi
    
    # 배포 패키지 생성
    mkdir -p deploy-package
    cp "$JAR_FILE" deploy-package/
    cp database/oracle_schema.sql deploy-package/ 2>/dev/null || cp oracle_schema.sql deploy-package/
    
    # VPS 시작 스크립트 생성
    cat > deploy-package/start-server.sh << 'EOF'
#!/bin/bash

echo "🚀 MBTI Test 서버 시작"

# 환경변수 확인
if [ -z "$AI_KEY" ] || [ -z "$DB_URL" ] || [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ]; then
    echo "❌ 환경변수를 설정하세요:"
    echo "export AI_KEY='your_api_key'"
    echo "export DB_URL='jdbc:oracle:thin:@your-db:1521:XE'"
    echo "export DB_USERNAME='your_username'"
    echo "export DB_PASSWORD='your_password'"
    exit 1
fi

# DB 초기화 (선택사항)
if [ "$1" = "--init-db" ]; then
    echo "🗄️  DB 초기화 중..."
    DB_HOST=$(echo $DB_URL | sed -n 's/.*@\([^:]*\):.*/\1/p')
    DB_PORT=$(echo $DB_URL | sed -n 's/.*:\([0-9]*\)\/.*/\1/p')
    DB_SID=$(echo $DB_URL | sed -n 's/.*\/\(.*\)/\1/p')
    sqlplus -S "$DB_USERNAME/$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_SID" @oracle_schema.sql
    echo "✅ DB 초기화 완료"
fi

# 서버 시작
echo "🎯 애플리케이션 시작 중..."
export SPRING_PROFILES_ACTIVE=cloud
nohup java -jar *.jar > app.log 2>&1 &
echo "✅ 서버가 백그라운드에서 시작되었습니다."
echo "📋 로그 확인: tail -f app.log"
echo "🌐 앱 URL: http://your-server:8080"
EOF
    
    chmod +x deploy-package/start-server.sh
    
    echo -e "${GREEN}✅ VPS 배포 패키지 준비 완료: deploy-package/${NC}"
    echo ""
    echo -e "${YELLOW}📋 VPS 서버 배포 방법:${NC}"
    echo "1. 패키지 업로드:"
    echo "   scp -r deploy-package user@your-server:~/"
    echo ""
    echo "2. 서버에서 환경변수 설정:"
    echo "   export AI_KEY='$AI_KEY'"
    echo "   export DB_URL='실제DB주소'"
    echo "   export DB_USERNAME='실제사용자명'"
    echo "   export DB_PASSWORD='실제비밀번호'"
    echo ""
    echo "3. 서버 시작:"
    echo "   cd deploy-package"
    echo "   ./start-server.sh --init-db  # DB 초기화와 함께 시작"
    echo "   또는"
    echo "   ./start-server.sh            # DB 초기화 없이 시작"
}

# 배포 후 검증
verify_deployment() {
    echo -e "${YELLOW}🔍 배포 검증 중...${NC}"
    
    case $DEPLOY_TYPE in
        "docker")
            sleep 10  # 컨테이너 시작 대기
            if curl -s http://localhost:8080/health > /dev/null; then
                echo -e "${GREEN}✅ 애플리케이션이 정상 작동 중입니다.${NC}"
            else
                echo -e "${RED}❌ 애플리케이션 접근 실패${NC}"
                echo "docker logs mbtitest 명령으로 로그를 확인하세요."
            fi
            ;;
        *)
            echo -e "${YELLOW}💡 수동으로 애플리케이션 상태를 확인하세요.${NC}"
            ;;
    esac
}

# 메인 실행 로직
main() {
    check_env_vars
    
    # Docker가 아닌 경우에만 DB 초기화
    if [[ "$DEPLOY_TYPE" != "docker" ]]; then
        init_database
    fi
    
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
    
    verify_deployment
}

# 스크립트 실행
main

echo -e "${GREEN}🎉 배포 프로세스 완료!${NC}"
echo -e "${YELLOW}💡 문제가 발생하면 다음을 확인하세요:${NC}"
echo "1. DB 연결 정보가 정확한지"
echo "2. 환경변수가 올바르게 설정되었는지"
echo "3. 네트워크 방화벽 설정"