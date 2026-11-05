FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null
COPY src src
RUN ./gradlew --no-daemon clean bootWar -x test

FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul
RUN groupadd -r app && useradd -r -g app app && mkdir -p /app/uploads /app/logs && chown -R app:app /app
WORKDIR /app
COPY --from=build --chown=app:app /src/build/libs/*.war app.war
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.war"]