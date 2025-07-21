# 🌐 네이버클라우드 플랫폼 배포 완벽 가이드

## 🚀 빠른 시작

### 1단계: 환경 설정
```bash
# API 키 환경변수 설정
export AI_KEY="your_actual_gemini_api_key_here"
```

### 2단계: 배포 실행
```bash
# 선택한 방식으로 배포
./deploy.sh app-engine    # 서버리스 (추천)
./deploy.sh docker        # 컨테이너
./deploy.sh vps          # 가상서버
```

---

## 📋 상세 배포 방법

### 🔥 방법 1: Cloud Functions (서버리스) - 가장 간단! ⭐

**장점:**
- 무료 할당량 제공
- 자동 스케일링
- 관리 불필요
- 트래픽이 없으면 요금 0원

**단계:**
1. [네이버클라우드](https://www.ncloud.com/) 계정 생성
2. Console > Application Service > Cloud Functions 선택
3. 함수 생성:
   ```yaml
   # function.yaml
   name: mbti-test
   runtime: java11
   memory: 512MB
   timeout: 30s
   environment:
     AI_KEY: "실제_API_키"
   ```
4. JAR 파일 업로드 및 배포

**예상 비용:** 월 10만 요청까지 무료, 이후 요청당 0.0002원

---

### 🐳 방법 2: Container Registry + Cloud Run

**장점:**
- Docker 기반으로 이식성 좋음
- CI/CD 파이프라인 구축 가능
- 세밀한 리소스 제어

**배포 명령어:**
```bash
# 1. 프로젝트 빌드
./gradlew build

# 2. Docker 이미지 생성
docker build -t mbtitest .

# 3. NCP Container Registry 로그인
docker login your-registry.nvcr.ntruss.com

# 4. 이미지 태그 및 푸시
docker tag mbtitest your-registry.nvcr.ntruss.com/mbtitest:latest
docker push your-registry.nvcr.ntruss.com/mbtitest:latest

# 5. Cloud Run 배포 (NCP CLI 사용)
ncp cloudrun deploy mbtitest \
  --image your-registry.nvcr.ntruss.com/mbtitest:latest \
  --port 8080 \
  --env AI_KEY="실제_API_키" \
  --env SPRING_PROFILES_ACTIVE=prod
```

**예상 비용:** CPU 0.25/메모리 512MB 기준 월 약 15,000원

---

### 🖥️ 방법 3: Virtual Private Server (VPS)

**장점:**
- 전체 제어권
- 다른 서비스와 함께 운영 가능
- 예측 가능한 고정 비용

**서버 설정 과정:**

1. **VPS 인스턴스 생성**
   ```bash
   # NCP Console에서 생성 또는 CLI 사용
   ncp server create \
     --name mbtitest-server \
     --image ubuntu-20.04 \
     --flavor compact-1g \
     --key-name your-keypair
   ```

2. **서버 환경 구성**
   ```bash
   # SSH 접속
   ssh -i your-key.pem ubuntu@your-server-ip
   
   # Java 11 설치
   sudo apt update
   sudo apt install openjdk-11-jdk -y
   
   # 방화벽 설정
   sudo ufw allow 8080
   sudo ufw allow ssh
   sudo ufw --force enable
   
   # 애플리케이션 디렉토리 생성
   mkdir ~/mbtitest && cd ~/mbtitest
   ```

3. **애플리케이션 배포**
   ```bash
   # 로컬에서 JAR 파일 업로드
   scp -i your-key.pem build/libs/*.jar ubuntu@your-server-ip:~/mbtitest/
   
   # 서버에서 실행
   export AI_KEY="실제_Gemini_API_키"
   export SPRING_PROFILES_ACTIVE=prod
   nohup java -jar *.jar > app.log 2>&1 &
   ```

4. **도메인 연결** (선택사항)
   ```bash
   # Global DNS에서 도메인 구매 후 A 레코드 설정
   # your-domain.com -> your-server-ip
   
   # SSL 인증서 설정 (Let's Encrypt)
   sudo apt install certbot -y
   sudo certbot certonly --standalone -d your-domain.com
   ```

**예상 비용:** Standard-1 (1CPU/1GB) 기준 월 약 8,000원

---

## 🛡️ 보안 설정

### API 키 보안 강화
1. **NCP Secret Manager 사용**
   ```bash
   # Secret 생성
   ncp secrets create ai-api-key --secret-string "실제_API_키"
   
   # 애플리케이션에서 참조
   AI_KEY=$(ncp secrets get-secret-value --secret-id ai-api-key --query SecretString --output text)
   ```

2. **IAM 역할 기반 접근 제어**
   - 최소 권한 원칙 적용
   - API 키 접근을 특정 서비스로만 제한

### HTTPS 강제 설정
```properties
# application-prod.properties
server.ssl.enabled=true
security.require-ssl=true
server.port=443
```

---

## 📊 모니터링 및 로깅

### 1. Cloud Log Analytics 연동
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="NAVER_CLOUD" class="com.navercorp.cloud.logging.CloudLogAppender">
        <project-id>your-project-id</project-id>
        <log-name>mbtitest-logs</log-name>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="NAVER_CLOUD"/>
    </root>
</configuration>
```

### 2. 헬스체크 모니터링
```bash
# 서버 상태 확인 스크립트
curl -f http://your-app-url/health || echo "App is down!" | mail -s "MBTI App Alert" admin@yourmail.com
```

---

## 💰 비용 비교표

| 방식 | 월 예상 비용 | 장점 | 단점 |
|------|-------------|-----|-----|
| **Cloud Functions** | ~5,000원 | 관리 불필요, 무료 할당량 | 콜드 스타트 |
| **Container + Cloud Run** | ~15,000원 | 확장성, CI/CD | 컨테이너 지식 필요 |
| **VPS Standard-1** | ~8,000원 | 전체 제어, 고정비용 | 관리 필요 |

---

## 🔧 트러블슈팅

### 일반적인 문제들

1. **API 키 인식 안됨**
   ```bash
   # 환경변수 확인
   echo $AI_KEY
   
   # 애플리케이션 로그 확인
   curl http://your-app/health
   ```

2. **포트 충돌**
   ```bash
   # 사용중인 포트 확인
   netstat -tulpn | grep :8080
   
   # 프로세스 종료
   kill -9 $(lsof -t -i:8080)
   ```

3. **메모리 부족**
   ```bash
   # JVM 힙 크기 조정
   java -Xms256m -Xmx512m -jar app.jar
   ```

### 지원 및 문의
- 네이버클라우드 플랫폼 고객센터: 1588-3820
- 기술 문서: https://guide.ncloud-docs.com
- 커뮤니티: https://www.ncloud.com/community

---

## 🎉 배포 완료 체크리스트

- [ ] ✅ API 키 환경변수 설정
- [ ] ✅ 애플리케이션 빌드 성공
- [ ] ✅ 선택한 방식으로 배포 완료
- [ ] ✅ `/health` 엔드포인트 응답 확인
- [ ] ✅ MBTI 테스트 기능 동작 확인
- [ ] ✅ HTTPS 설정 (운영 환경)
- [ ] ✅ 모니터링 및 알림 설정
- [ ] ✅ 백업 및 복구 계획 수립

**🚀 축하합니다! MBTI 테스트 앱이 네이버클라우드에 성공적으로 배포되었습니다!**
