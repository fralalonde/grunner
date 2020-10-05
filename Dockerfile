FROM openjdk:14-jdk-alpine
RUN addgroup spring && adduser -D --ingroup spring spring
USER spring
ARG JAR_FILE
ADD target/${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]