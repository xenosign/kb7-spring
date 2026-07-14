# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
# 윈도우 체크아웃에서 CRLF로 바뀐 gradlew 대응
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew --version
COPY src ./src
RUN ./gradlew war --no-daemon -x test

FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /workspace/build/libs/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
