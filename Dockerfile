FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25
COPY target/prim-brukerstyring*.jar /app.jar
ENV RETRY_MAX_RETRIES=5
ENV RETRY_INTERVAL=5000
ENV SERVER_PORT=8080
ENV JAVA_OPTS='--enable-preview'
ENTRYPOINT ["java", "-jar", "/app.jar"]
