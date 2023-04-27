#!/bin/bash

# ***** Source global variables ****
source ~/.env_profile

echo "*************************"
echo "HOME_PATH: ${GLOBAL_HOME_PATH}"
echo "*************************"

verify=""

if [[ "${GLOBAL_HOME_PATH}" == "${verify}" ]]; then 
    cd $(pwd)/../metrics/experiment-runner
    echo "Current path: $(pwd)"
else
    cd ${GLOBAL_HOME_PATH}/../metrics/experiment-runner
    echo "Current path: $(pwd)"
fi 

# ***** Source experiment-runner configuration ****
source ./.env

if [[ "${GLOBAL_SESSION_ID}" == "${verify}" ]]; then

    echo "**************************"
    echo "${output_session_id}"
    echo "Verify the session ID ${output_session_id}"
    echo "and then press any key to proceed."
    echo "**************************"
    read anykey
    echo "**************************"
    echo "Start experiment-runner"
    echo "**************************"
    python3 exp-runner.py
else
    echo "Session ID: ${GLOBAL_SESSION_ID}"
    export output_session_id=${GLOBAL_SESSION_ID}
    echo "**************************"
    echo "Start experiment-runner"
    echo "**************************"
    python3 exp-runner.py
fi
