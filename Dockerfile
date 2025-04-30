FROM gcr.io/distroless/java21-debian12
COPY target/prim-brukerstyring*.jar /app.jar
ENV RETRY_MAX_RETRIES=5
ENV RETRY_INTERVAL=5000
ENV SERVER_PORT=8080
ENV JAVA_OPTS='--enable-preview'
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
