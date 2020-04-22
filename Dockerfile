FROM openjdk:8-alpine

COPY target/uberjar/clj-sms.jar /clj-sms/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/clj-sms/app.jar"]
