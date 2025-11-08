# Multi-stage build for Spring Boot optimization

# Stage 1: Dependencies cache
FROM gradle:8.10-jdk21 AS cache

WORKDIR /app

# Gradle wrapper 및 설정 파일 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# 의존성 다운로드 (캐싱 최적화)
RUN gradle dependencies --no-daemon || true

# Stage 2: Builder
FROM gradle:8.10-jdk21 AS builder

WORKDIR /app

# 캐시된 의존성 복사
COPY --from=cache /root/.gradle /root/.gradle
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle ./gradle

# 소스 코드 복사
COPY src ./src

# 빌드 (테스트 제외)
RUN gradle clean build -x test --no-daemon

# Stage 3: Runtime (최종 이미지)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 보안을 위한 non-root 유저 생성
RUN addgroup -S spring && adduser -S spring -G spring

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 소유권 변경
RUN chown spring:spring app.jar

USER spring:spring

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# JVM 메모리 최적화 옵션 추가
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]

# 프로파일은 docker-compose에서 환경 변수로 설정
