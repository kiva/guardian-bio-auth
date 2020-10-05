FROM openjdk:8-jdk-alpine

RUN mkdir /app
WORKDIR /app
COPY build/libs/identityservice-0.0.1-SNAPSHOT.jar ./app.jar
COPY lib/dd-java-agent.jar .
RUN adduser -S app
USER app 
ENTRYPOINT ["java","-javaagent:./dd-java-agent.jar","-Djava.security.egd=file:/dev/./urandom", "-jar","./app.jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000s"]
