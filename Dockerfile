FROM eclipse-temurin:21-jdk AS build

WORKDIR /code

COPY kotlin kotlin.bat project.yaml libs.versions.toml ./
RUN chmod +x kotlin

COPY app/ ./app/
COPY modules/ ./modules/
COPY build-plugins/ ./build-plugins/

RUN ./kotlin package

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl jq && adduser -D --no-create-home parcelview

COPY --chown=parcelview:parcelview --from=build /code/build/tasks/_app_executableJarJvm/*.jar /app/app.jar

USER parcelview

ARG SERVER_PORT=2320
EXPOSE ${SERVER_PORT}

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -s http://localhost:${SERVER_PORT}/api/v1/status | jq -e '.status == "UP"' > /dev/null || exit 1

# Standard setup: courier credentials (e.g. FEDEX_CLIENT_ID, USPS_CLIENT_ID) must be
# supplied as environment variables, e.g. via compose.yaml's `env_file`.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
