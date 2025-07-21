# Multi-stage build를 사용하여 최적화된 이미지 생성

# Build stage
FROM gradle:7.6-jdk11 AS build
WORKDIR /app

# Gradle 설정 파일 복사
COPY build.gradle settings.gradle ./
COPY gradle/ gradle/

# 소스 코드 복사
COPY src/ src/

# 애플리케이션 빌드
RUN ./gradlew build -x test

# Runtime stage
FROM openjdk:11-jre-slim

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 설정
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# 환경변수 설정 (기본값)
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# JVM 옵션 설정 (메모리 최적화)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
