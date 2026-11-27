# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
COPY config config
COPY shared shared
COPY api api
RUN mkdir app

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :api:installDist --no-daemon --console=plain

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S delivery && adduser -S delivery -G delivery
WORKDIR /app
COPY --from=build /src/api/build/install/api/ .
USER delivery

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["bin/api"]
