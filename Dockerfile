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

FROM tomcat:8

# Install maven
RUN apt-get update && \
    apt-get install -y maven openjdk-8-jdk

WORKDIR /code

# Prepare by downloading dependencies
ADD pom.xml /code/pom.xml
ADD src /code/src
ADD perseo_core-entrypoint.sh /code/src/

RUN mvn dependency:resolve && \
    mvn verify && \
    mvn package && \
    rm -rf /usr/local/tomcat/webapps/* && \
    cp target/perseo-core-*.war /usr/local/tomcat/webapps/perseo-core.war && \
    mvn clean && \
    apt-get clean && \
    apt-get remove -y maven && \
    apt-get -y autoremove && \
    rm -rf /var/lib/apt/lists/* && \
    rm -rf /code/src

EXPOSE 8080

ENTRYPOINT ["/code/src/perseo_core-entrypoint.sh"]