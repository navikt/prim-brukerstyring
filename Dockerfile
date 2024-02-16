FROM ghcr.io/navikt/baseimages/temurin:21

ENV RETRY_MAX_RETRIES=5
ENV RETRY_INTERVAL=5000
ENV SERVER_PORT=8080
ENV JAVA_OPTS --enable-preview
COPY target/prim-brukerstyring*.jar /app/app.jar