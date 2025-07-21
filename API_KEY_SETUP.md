# 🔐 API 키 설정 안내

## 📋 개요
이 프로젝트는 Google Gemini API를 사용하여 성격 분석을 수행합니다.
보안을 위해 API 키는 서버에서만 관리되며, 클라이언트에서는 노출되지 않습니다.

## 🚀 로컬 개발 환경 설정

### 방법 1: application-local.properties 사용 (권장)
1. `src/main/resources/application-local.properties` 파일을 생성
2. 다음 내용으로 설정:
```properties
AI_KEY=YOUR_ACTUAL_GEMINI_API_KEY
```
3. Spring Boot 실행 시 `--spring.profiles.active=local` 옵션 추가

### 방법 2: 환경변수 사용
```bash
export AI_KEY=YOUR_ACTUAL_GEMINI_API_KEY
java -jar mbtitest.jar
```

### 방법 3: 실행 시 파라미터 전달
```bash
java -jar mbtitest.jar --AI_KEY=YOUR_ACTUAL_GEMINI_API_KEY
```

## 🌐 서버 배포 설정

### Docker 환경
```dockerfile
ENV AI_KEY=your_actual_key_here
```

### 클라우드 플랫폼 (Heroku, AWS 등)
환경변수 설정에서 `AI_KEY` 추가

### 일반 서버
```bash
# .bashrc 또는 .profile에 추가
export AI_KEY=your_actual_key_here
```

## 🔑 Gemini API 키 발급 방법

1. [Google AI Studio](https://makersuite.google.com/app/apikey) 접속
2. 구글 계정으로 로그인
3. "Create API Key" 클릭
4. 생성된 키를 위의 설정 방법 중 하나로 적용

## ⚠️ 보안 주의사항

✅ **안전한 방법:**
- 환경변수 사용
- application-local.properties 사용 (Git 제외)
- 서버 설정 파일 사용

❌ **위험한 방법:**
- 코드에 직접 하드코딩
- 클라이언트 사이드에서 API 키 노출
- Git 저장소에 키 포함

## 🏗️ 현재 보안 구조

```
클라이언트 (브라우저)
    ↓ [답변 데이터만 전송]
서버 (Spring Boot)
    ↓ [API 키로 Gemini 호출]
Google Gemini API
    ↓ [분석 결과 반환]
서버
    ↓ [결과만 반환]
클라이언트 (결과 표시)
```

**API 키는 서버에서만 사용되며, 브라우저에서는 완전히 숨겨집니다!** 🔒
