ARG RUNTIME_IMAGE=eclipse-temurin:24-jre

FROM ${RUNTIME_IMAGE}

WORKDIR /app
USER nobody
COPY target/project-1.0.jar .

ENTRYPOINT ["java", "-cp", "project-1.0.jar", "academy.Application"]
