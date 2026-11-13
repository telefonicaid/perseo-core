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

FROM tomcat:9.0-jdk17-temurin-jammy

ARG GITHUB_ACCOUNT=telefonicaid
ARG GITHUB_REPOSITORY=perseo-core

# Install maven

WORKDIR /code

# Prepare by downloading dependencies
COPY pom.xml /code/pom.xml
COPY perseo-main /code/perseo-main/
COPY perseo-utils /code/perseo-utils/
COPY perseo_core-entrypoint.sh /code
COPY logging.properties.template /usr/local/tomcat/conf/logging.properties

# The base Tomcat image enables a file-based access log by default.
RUN sed -i '/<Valve className="org.apache.catalina.valves.AccessLogValve"/,/\/>/d' /usr/local/tomcat/conf/server.xml

# hadolint ignore=DL3005,DL3008
RUN apt-get -y update && \
    apt-get -y upgrade && \
    apt-get install --no-install-recommends -y maven curl && \
    mvn -B clean verify && \
    rm -rf /usr/local/tomcat/webapps/* && \
    cp perseo-main/target/perseo-main-*.war /usr/local/tomcat/webapps/perseo-core.war && \
    mkdir -p /usr/local/tomcat/webapps/perseo-core && \
    mvn clean && \
    apt-get clean && \
    apt-get remove -y maven && \
    apt-get -y autoremove && \
    rm -rf /var/lib/apt/lists/* && \
    rm -rf /code/src

WORKDIR /usr/local/tomcat/webapps/perseo-core

RUN jar xf ../perseo-core.war && \
    rm ../perseo-core.war && \
    chown -R root:root /usr/local/tomcat/webapps && \
    chmod -R a-w /usr/local/tomcat/webapps

WORKDIR /code

RUN mkdir -p /var/log/perseo /usr/local/tomcat/temp /usr/local/tomcat/work /usr/local/tomcat/logs && \
    chown -R 1000:1000 /var/log/perseo /usr/local/tomcat/temp /usr/local/tomcat/work /usr/local/tomcat/logs && \
    chmod 0750 /var/log/perseo /usr/local/tomcat/temp /usr/local/tomcat/work /usr/local/tomcat/logs && \
    chmod 0555 /code/perseo_core-entrypoint.sh

LABEL "maintainer"="FIWARE Perseo Team. Telefónica I+D"
LABEL "org.opencontainers.image.authors"="iot_support@tid.es"
LABEL "org.opencontainers.image.documentation"="https://perseo.readthedocs.io/"
LABEL "org.opencontainers.image.vendor"="Telefónica Investigación y Desarrollo, S.A.U"
LABEL "org.opencontainers.image.licenses"="GPL-2.0"
LABEL "org.opencontainers.image.title"="Complex Event Processing component for NGSI-v2 (Backend) "
LABEL "org.opencontainers.image.description"="An Esper-based Complex Event Processing (CEP) software designed to be fully NGSIv2-compliant."
LABEL "org.opencontainers.image.source"="https://github.com/${GITHUB_ACCOUNT}/${GITHUB_REPOSITORY}"

EXPOSE 8080

HEALTHCHECK CMD curl --fail http://localhost:8080/perseo-core/version || exit 1

ENV HOME=/tmp

USER 1000:1000

ENTRYPOINT ["/code/perseo_core-entrypoint.sh"]
