# back/Dockerfile

# Build stage: Gradle 8.12 with JDK 21
FROM gradle:8.12-jdk21 as builder
WORKDIR /home/gradle/project
COPY . .
# 환경변수는 빌드 시점에 필요하지 않은 경우 런타임에 전달해도 됩니다.
RUN gradle clean build --no-daemon

# Runtime stage: OpenJDK 21 (가능하다면 slim 버전 사용)
FROM openjdk:21
WORKDIR /app
# 빌드된 jar 파일이 build/libs 디렉토리에 있다고 가정합니다.
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
