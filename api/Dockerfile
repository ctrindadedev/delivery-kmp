FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY build/install/api/ .

EXPOSE 8080
ENTRYPOINT ["bin/api"]
