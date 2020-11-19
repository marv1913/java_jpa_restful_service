# ./Dockerfile

FROM maven as maven_builder
WORKDIR /app
ADD . .
RUN mvn clean package
FROM tomcat:9.0.40-jdk11-openjdk-buster

WORKDIR /home
EXPOSE 8080

RUN ls
ENV SONGS_PASSWORD=admin
COPY --from=maven_builder /app/songsservlet/target /home
RUN cd /home && ls
RUN cp /home/songsservlet-MarvEn.war /usr/local/tomcat/webapps


# start tomcat
CMD ["catalina.sh", "run"]
