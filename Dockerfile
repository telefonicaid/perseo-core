FROM centos:6

WORKDIR /opt

ENV JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk.x86_64

RUN yum update -y && yum install -y wget \
  && wget https://dl.fedoraproject.org/pub/epel/epel-release-latest-6.noarch.rpm && yum localinstall -y --nogpgcheck epel-release-latest-6.noarch.rpm \
  && yum install -y npm git unzip java-1.7.0-openjdk java-1.7.0-openjdk-devel tomcat \
  && wget -c http://ftp.cixug.es/apache/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.zip && unzip -oq apache-maven-3.2.5-bin.zip \
  && cp -rf apache-maven-3.2.5 /opt/maven && ln -fs /opt/maven/bin/mvn /usr/bin/mvn \
  && git clone https://github.com/telefonicaid/perseo-core.git && cd perseo-core && mvn package -Dmaven.test.skip=true \
  && mv target/perseo-core-*.war /usr/share/tomcat/webapps/perseo-core.war \
  && mkdir /var/log/perseo && chown tomcat:tomcat /var/log/perseo

EXPOSE 8080

CMD service tomcat start && tail -f /var/log/tomcat/catalina.out
