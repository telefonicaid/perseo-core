#!/bin/bash

PERSEO_FE_URL=172.17.0.1:19090

PERSEO_URL_ARG=${1}
PERSEO_URL_VALUE=${2}
if [ "$PERSEO_URL_ARG" == "-perseo_fe_url" ]; then
    sed -i 's/'$PERSEO_FE_URL'/'$PERSEO_URL_VALUE'/g' /etc/perseo-core.properties;
fi

service tomcat start && tail -f /var/log/tomcat/catalina.out
