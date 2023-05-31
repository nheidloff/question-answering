#!/bin/bash

# ***** Source global variables ****
source ~/.env_profile

echo "*************************"
echo "HOME_PATH: ${GLOBAL_HOME_PATH}"
echo "*************************"

verify=""

if [[ "${GLOBAL_HOME_PATH}" == "${verify}" ]]; then
    cd $(pwd)/../service
    echo "Current path: $(pwd)"
    export CURRENT_PATH=$(pwd)
else
    cd ${GLOBAL_HOME_PATH}/../service
    echo "Current path: $(pwd)"
    export CURRENT_PATH=$(pwd)
fi

# ***** Source qa service configuration ****
source ./.env

verify=""

if [[ "${GLOBAL_SESSION_ID}" == "${verify}" ]]; then

    echo "**************************"
    echo $EXPERIMENT_METRICS_SESSION
    echo "Please copy and past the $EXPERIMENT_METRICS_SESSION" 
    echo "into the '.env' file for the 'experiment-runner'." 
    echo "Then press any key to proceed."
    echo "**************************"
    read anykey
    echo "**************************"
    echo "Start QA- Service"
    echo "**************************"
    mvn package
    mvn quarkus:dev
else
    echo "Session ID: ${GLOBAL_SESSION_ID}"
    cd ${CURRENT_PATH}/../metrics/output
    export METRICS_DIR=$(pwd)
    echo "METRICS_DIR: ${METRICS_DIR}"
    cd ${CURRENT_PATH}
    export EXPERIMENT_METRICS_DIRECTORY=${METRICS_DIR}
    export EXPERIMENT_METRICS_SESSION=${GLOBAL_SESSION_ID}
    echo "**************************"
    echo "Start QA- Service"
    echo "**************************"
    mvn package
    mvn quarkus:dev
fi

