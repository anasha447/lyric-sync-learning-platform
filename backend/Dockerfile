# syntax=docker/dockerfile:1

# ---- build: compile the jar and split it into layers ----
FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /src

# dependencies change less often than code, so they get their own cached layer
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests \
 && java -Djarmode=tools -jar target/lyricsync-backend-*.jar extract --layers --launcher --destination /extracted

# ---- runtime: JRE only, non-root ----
FROM eclipse-temurin:24-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app

COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
