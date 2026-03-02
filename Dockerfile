# Usamos apenas o JRE (Runtime), que é menor e mais seguro
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o JAR que você gerou com sucesso no seu Mac
# O caminho padrão do Gradle é build/libs/
COPY build/libs/*.jar app.jar

EXPOSE 8080

# Configurações de performance para o container
ENTRYPOINT ["java", "-Xmx512m", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]