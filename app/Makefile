.PHONY: build

setup:
	./gradlew wrapper --gradle-version 8.4

build:
	./gradlew clean build

install:
	./gradlew clean installDist

lint:
	./gradlew checkstyleMain

test:
	./gradlew test

check-deps:
	./gradlew dependencyUpdates -Drevision=release

report:
	./gradlew test jacocoTestReport

run:
	./build/install/app/bin/app