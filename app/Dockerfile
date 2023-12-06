FROM eclipse-temurin:20-jdk
LABEL name="san-page-analyzer"
ARG GRADLE_VERSION=8.2
RUN apt-get update && apt-get install -yq make unzip
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip \
    && unzip gradle-${GRADLE_VERSION}-bin.zip \
    && rm gradle-${GRADLE_VERSION}-bin.zip
ENV GRADLE_HOME=/opt/gradle
ENV PORT=8080
RUN mv gradle-${GRADLE_VERSION} ${GRADLE_HOME}
ENV PATH=$PATH:$GRADLE_HOME/bin
WORKDIR app
COPY ./ .
ENTRYPOINT ["./gradlew", "run"]
EXPOSE $PORT:7070



