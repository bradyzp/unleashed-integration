FROM maven:3.6.3-jdk-14 as build
VOLUME /root/.m2
WORKDIR /build
COPY pom.xml .
COPY src ./src
COPY settings.xml .
RUN mvn -B -s settings.xml dependency:resolve package

FROM adoptopenjdk:14-jre-hotspot
RUN mkdir /opt/app
COPY --from=build /build/target/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
