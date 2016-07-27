FROM centos:6

WORKDIR /opt

ENV JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk.x86_64
ENV PERSEO_FE_URL=PERSEO_FE_ENDPOINT

COPY . /opt/perseo-core
WORKDIR /opt/perseo-core

RUN yum update -y && yum install -y wget \
  && wget https://dl.fedoraproject.org/pub/epel/epel-release-latest-6.noarch.rpm && yum localinstall -y --nogpgcheck epel-release-latest-6.noarch.rpm \
  && yum install -y npm git unzip java-1.7.0-openjdk java-1.7.0-openjdk-devel tomcat \
  && wget -c http://ftp.cixug.es/apache/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.zip && unzip -oq apache-maven-3.2.5-bin.zip \
  && cp -rf apache-maven-3.2.5 /opt/maven && ln -fs /opt/maven/bin/mvn /usr/bin/mvn \
  && mvn package -Dmaven.test.skip=true \
  && mv target/perseo-core-*.war /usr/share/tomcat/webapps/perseo-core.war \
  && mkdir /var/log/perseo && chown tomcat:tomcat /var/log/perseo \
  && echo "# This file should be copied by deployment process" > /etc/perseo-core.properties \
  && echo "# into /etc/perseo-core.properties, with the appropiate permissions" >>  /etc/perseo-core.properties \
  && echo "" >>  /etc/perseo-core.properties \
  && echo "# URL for invoking actions when a rule is fired" >>  /etc/perseo-core.properties \
  && echo "action.url = http://${PERSEO_FE_URL}/actions/do" >>  /etc/perseo-core.properties \
  && echo "" >>  /etc/perseo-core.properties \
  && echo "# Time in milliseconds (long) to \"expire\" a \"dangling\" rule" >>  /etc/perseo-core.properties \
  && echo "rule.max_age= 60000" >>  /etc/perseo-core.properties

EXPOSE 8080

ENTRYPOINT ["/opt/perseo-core/perseo_core-entrypoint.sh"]
