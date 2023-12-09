.PHONY: build

build:
	./gradlew clean build

install:
	./gradlew clean installDist

lint:
	./gradlew checkstyleMain

test:
	./gradlew test

report:
	./gradlew test jacocoTestReport

image:
	docker build -t sanmoskalenko/page-analazer:latest .