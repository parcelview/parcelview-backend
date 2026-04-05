# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /code

COPY amper amper.bat project.yaml libs.versions.toml ./
RUN chmod +x amper

COPY  app/ ./app/
COPY modules/ ./modules/
COPY build-plugins/ ./build-plugins/

RUN ./amper package

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y curl jq && rm -rf /var/lib/apt/lists/*

WORKDIR /code

COPY --from=build /code/build/tasks/_app_executableJarJvm/*.jar app.jar

ARG SERVER_PORT=2320
EXPOSE ${SERVER_PORT}

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -s http://localhost:${SERVER_PORT}/api/v1/status | jq -e '.status == "UP"' > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
