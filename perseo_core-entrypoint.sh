#!/bin/bash
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

DEFAULT_PERSEO_FE_URL=perseo_fe_endpoint

PERSEO_FE_URL_ARG=${1}
PERSEO_FE_URL_VALUE=${2}
if [ "$PERSEO_FE_URL_ARG" == "-perseo_fe_url" ] && [ -z "$PERSEO_FE_URL" ]
then
    export PERSEO_FE_URL="$PERSEO_FE_URL_VALUE"
fi

mkdir -p /var/log/perseo
touch /var/log/perseo/perseo-core.log

tail -f /var/log/perseo/perseo-core.log &

# We use tomcat from Apache, then will be started using catalina.sh, instead service tomcat

exec ${CATALINA_HOME}/bin/catalina.sh run
