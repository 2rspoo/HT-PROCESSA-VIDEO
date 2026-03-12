# Estágio 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copia as configurações de dependências
COPY pom.xml .

# Copia o código fonte (apenas uma vez)
COPY src ./src

# Executa o build (O -U força a atualização das libs AWS se necessário)
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -U

# Estágio 2: Runtime
FROM eclipse-temurin:21-jre-alpine
# Garante o FFmpeg instalado para o processador de vídeo
RUN apk add --no-cache ffmpeg
WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]