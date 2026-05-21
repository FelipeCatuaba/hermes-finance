## Dependencies stage
FROM eclipse-temurin:21-jdk-alpine as dependencies

# Deixa o tamanho da imagem menor, removendo o cache do apk
RUN apk add --no-cache maven

WORKDIR /build
COPY pom.xml .
# Baixa as dependências do projeto para cachear essa etapa
RUN mvn dependency:go-offline

## Build stage
FROM dependencies as builder
COPY src ./src
RUN mvn clean package -DskipTests

## Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]