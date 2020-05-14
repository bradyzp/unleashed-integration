FROM maven:3.6.3-jdk-14 as build
VOLUME /root/.m2
WORKDIR /build
COPY pom.xml .
COPY src .
COPY settings.xml .
RUN mvn -B -f pom.xml -s settings.xml dependency:resolve
RUN mvn -s settings.xml package

FROM adoptopenjdk:14-jre-hotspot
RUN mkdir /opt/app
COPY --from=build /build/target/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
