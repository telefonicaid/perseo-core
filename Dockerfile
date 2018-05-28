FROM tomcat:8

# Install maven
RUN apt-get update && \
    apt-get install -y maven openjdk-8-jdk

WORKDIR /code

# Prepare by downloading dependencies
ADD pom.xml /code/pom.xml
ADD src /code/src

RUN mvn dependency:resolve && \
    mvn verify && \
    mvn package && \
    cp target/perseo-core-*.war /usr/local/tomcat/webapps/

EXPOSE 8080