FROM gradle:8.2-jdk AS TEMP_BUILD_IMAGE

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

COPY build.gradle.kts settings.gradle.kts $APP_HOME
COPY gradle $APP_HOME/gradle
COPY --chown=gradle:gradle . /home/gradle/wrapper
COPY . .

RUN ./gradlew clean build  \
    --no-daemon  \
    --parallel \
    --exclude-task test  \
    --exclude-task :checkstyleMain  \
    --exclude-task :checkstyleTest \
    --exclude-task :check \
    --exclude-task :testClasses

FROM bellsoft/liberica-openjdk-debian:latest
LABEL name="san-page-analyzer"

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

ENV ARTIFACT_NAME=app-20.jar
ENV MIGRATION_RESOURCES=src/main/resources/
ENV JTE_TEMPLATES_PATH=templates
ENV SCHEMA_PATH=migrations/schema.sql
ENV APP_PORT=8080
ENV JDBC_DATABASE_URL=jdbc:postgresql://db/postgres?user=postgres&password=secret
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .
COPY $MIGRATION_RESOURCES .
COPY src/main/resources/templates ./$JTE_TEMPLATES_PATH

EXPOSE $APP_PORT
ENTRYPOINT exec java -jar ${ARTIFACT_NAME}