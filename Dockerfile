#
# Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of perseo-core
#
# perseo-core is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
# General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
# option) any later version.
# perseo-core is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License along with perseo-core. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
#

FROM centos:centos7.4.1708

ENV MVN_VER "3.2.5"
ENV JAVA_VERSION "1.7.0"
ENV TOMCAT_VERSION "7.0.70"
ENV CATALINA_HOME "/opt/tomcat"
ENV PERSEO_FE_URL=perseo_fe_endpoint

COPY . /opt/perseo-core/
WORKDIR /opt/perseo-core

RUN yum update -y && \
  yum install -y epel-release && yum update -y epel-release && \
  yum install -y java-${JAVA_VERSION}-openjdk-devel && \
  export JAVA_HOME=/usr/lib/jvm/java-${JAVA_VERSION}-openjdk && \

  echo "INFO: Download and install Maven..." && \
  curl --remote-name --location --insecure --silent --show-error http://ftp.cixug.es/apache/maven/maven-${MVN_VER%%.*}/${MVN_VER}/binaries/apache-maven-${MVN_VER}-bin.tar.gz && \
  tar xzf apache-maven-${MVN_VER}-bin.tar.gz && \
  mv apache-maven-${MVN_VER} maven && mv maven /opt/maven && rm -f apache-maven-${MVN_VER}-bin.tar.gz && ln -fs /opt/maven/bin/mvn /usr/bin/mvn && \

  echo "INFO: Download and install Tomcat..." && \
  curl --remote-name --location --insecure --silent --show-error https://archive.apache.org/dist/tomcat/tomcat-${TOMCAT_VERSION%%.*}/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz && \
  tar xzf apache-tomcat-${TOMCAT_VERSION}.tar.gz && \
  mv apache-tomcat-${TOMCAT_VERSION} tomcat && mv tomcat ${CATALINA_HOME} && rm -f apache-tomcat-${TOMCAT_VERSION}.tar.gz && \
  useradd tomcat && rm -rf ${CATALINA_HOME}/logs && mkdir -p /var/log/tomcat && ln -fs /var/log/tomcat ${CATALINA_HOME}/logs && \

  echo "INFO: Build and install software..." && \
  mvn package -Dmaven.test.skip=true && \
  mv /opt/perseo-core/target/perseo-core-*.war ${CATALINA_HOME}/webapps/perseo-core.war && \
  mkdir /var/log/perseo && chown tomcat:tomcat /var/log/perseo && \
  echo "# This file should be copied by deployment process" > /etc/perseo-core.properties && \
  echo "# into /etc/perseo-core.properties, with the appropiate permissions" >>  /etc/perseo-core.properties && \
  echo "" >>  /etc/perseo-core.properties && \
  echo "# URL for invoking actions when a rule is fired" >>  /etc/perseo-core.properties && \
  echo "action.url = http://${PERSEO_FE_URL}/actions/do" >>  /etc/perseo-core.properties && \
  echo "" >>  /etc/perseo-core.properties && \
  echo "# Time in milliseconds (long) to \"expire\" a \"dangling\" rule" >>  /etc/perseo-core.properties && \
  echo "rule.max_age= 60000" >>  /etc/perseo-core.properties && \
  echo "INFO: Optimize Tomcat" && \
  echo "JAVA_OPTS=\"-Djava.awt.headless=true -Xmx512m -XX:MaxPermSize=256m -XX:+UseConcMarkSweepGC -Djava.library.path=/usr/lib64:/usr/lib -Djava.security.egd=file:/dev/./urandom\"" >> ${CATALINA_HOME}/conf/tomcat.conf && \

  echo "INFO: Cleaning unused software..." && \
  mvn clean && rm -rf /opt/maven && rm -rf ~/.m2 && \
  yum erase -y java-${JAVA_VERSION}-openjdk-devel libss && \
  echo "INFO: Java runtime not needs JAVA_HOME... Unsetting..." && \
  unset JAVA_HOME && \
  # Clean yum data
  yum clean all && rm -rf /var/lib/yum/yumdb && rm -rf /var/lib/yum/history && \
  # Erase without dependencies of the document formatting system (man). This cannot be removed using yum 
  # as yum uses hard dependencies and doing so will uninstall essential packages
  rpm -qa redhat-logos gtk2 pulseaudio-libs libvorbis jpackage* groff alsa* atk cairo libX* | xargs -r rpm -e --nodeps && \
  # Rebuild rpm data files
  rpm -vv --rebuilddb && \
  # Delete unused locales. Only preserve en_US and the locale aliases
  find /usr/share/locale -mindepth 1 -maxdepth 1 ! -name 'en_US' ! -name 'locale.alias' | xargs -r rm -r && \
  bash -c 'localedef --list-archive | grep -v -e "en_US" | xargs localedef --delete-from-archive' && \
  # We use cp instead of mv as to refresh locale changes for ssh connections
  # We use /bin/cp instead of cp to avoid any alias substitution, which in some cases has been problematic
  /bin/cp -f /usr/lib/locale/locale-archive /usr/lib/locale/locale-archive.tmpl && \
  build-locale-archive && \
  find /opt/perseo-core -name '.[^.]*' 2>/dev/null | xargs -r rm -rf && \
  # We don't need to manage Linux account passwords requisites: lenght, mays/mins, etc
  # This cannot be removed using yum as yum uses hard dependencies and doing so will uninstall essential packages
  rm -rf /usr/share/cracklib && \
  # We don't need glibc locale data
  # This cannot be removed using yum as yum uses hard dependencies and doing so will uninstall essential packages
  rm -rf /usr/share/i18n /usr/{lib,lib64}/gconv && \
  # We don't need wallpapers
  rm -rf /usr/share/wallpapers/* && \
  # Don't need old log files inside docker images
  rm -rf /tmp/* /var/log/*log

EXPOSE 8080

ENTRYPOINT ["/opt/perseo-core/perseo_core-entrypoint.sh"]

