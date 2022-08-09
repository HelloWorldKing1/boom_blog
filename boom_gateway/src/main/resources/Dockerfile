FROM java:alpine
VOLUME /tmp
ADD boom_gateway-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Xms256m","-Xmx256m","-jar","/app.jar"]
