FROM openjdk:11

COPY target /app

WORKDIR /app

RUN printf "spring:\n  application:\n    name: emulator-socket\n" > application.yaml


RUN useradd jbsv
RUN chown -R jbsv:jbsv /app
USER jbsv

CMD ["java","-Xmx2G","-jar","emulator-socket-1.0-SNAPSHOT.jar"]

