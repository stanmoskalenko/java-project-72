FROM gradle:8.2-jdk AS TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

COPY build.gradle.kts settings.gradle.kts $APP_HOME
COPY gradle $APP_HOME/gradle
COPY --chown=gradle:gradle . /home/gradle/wrapper
COPY . .

RUN ./gradlew clean build --no-daemon

FROM bellsoft/liberica-openjdk-debian:latest
LABEL name="san-page-analyzer"
ENV ARTIFACT_NAME=app-20-all.jar
ENV APP_HOME=/usr/app/
ENV MIGRATION_RESOURCES=src/main/resources/migrations/schema.sql
ENV JTE_TEMPLATES_PATH=templates
ENV SCHEMA_PATH=schema.sql
ENV APP_PORT=8080
WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .
COPY $MIGRATION_RESOURCES .
COPY src/main/resources/templates ./$JTE_TEMPLATES_PATH

EXPOSE $APP_PORT
ENTRYPOINT exec java -jar ${ARTIFACT_NAME}