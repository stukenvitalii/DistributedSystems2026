FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl lab3/auth-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/lab3/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENV SECURITY_JWT_SECRET=this-is-a-demo-secret-key-which-is-long-enough-123456
ENTRYPOINT ["java","-jar","/app/app.jar"]

