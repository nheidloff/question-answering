#!/bin/bash

source ~/.env_profile

echo "*************************"
echo "HOME_PATH: ${GLOBAL_HOME_PATH}"
echo "*************************"

verify=""

if [[ "${GLOBAL_HOME_PATH}" == "${verify}" ]]; then
    cd $(pwd)/../mass-mock
    echo "Current path: $(pwd)"
else
    cd ${GLOBAL_HOME_PATH}/../service
    echo "Current path: $(pwd)"
fi

echo "**************************"
echo "Start maas-mock"
echo "**************************"
mvn package
#mvn quarkus:dev
mvn quarkus:dev

