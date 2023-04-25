#!/bin/bash

# **************** Source Global variables
source ~/.env_profile

echo "************************************"
echo " Build and start with experiment-runner " 
echo " as a container and connect to Code Engine"
echo " $GLOBAL_QA_SERVICE_API_URL"
echo "************************************"

# ***************** Global variables
export HOME_PATH=$GLOBAL_HOME_PATH
export SESSION_ID=$GLOBAL_SESSION_ID
export QA_SERVICE_API_URL=$GLOBAL_QA_SERVICE_API_URL

function start_experiment_runner(){
    cd "${HOME_PATH}"/../metrics/experiment-runner/
    bash ./start_exp_container.sh
    cd "${HOME_PATH}"
}

start_experiment_runner
