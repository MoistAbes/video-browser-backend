FROM eclipse-temurin:21-jdk

# Instalacja FFmpeg i FFprobe
RUN apt-get update && apt-get install -y ffmpeg

WORKDIR /app

COPY target/zymflix-backend.jar .

EXPOSE 8080

CMD ["java", "-jar", "zymflix-backend.jar", "--spring.profiles.active=docker"]
