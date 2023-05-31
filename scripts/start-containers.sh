#!/bin/bash

# **************** Global variables
export HOME_PATH=$(pwd)
export SESSION_ID=$(date +%s)
export COMMIT_ID=""
export LOG_FOLDER=""

# **********************************************************************************
# Functions definition
# **********************************************************************************

function check_docker () {
    ERROR=$(docker ps 2>&1)
    RESULT=$(echo $ERROR | grep 'Cannot' | awk '{print $1;}')
    VERIFY="Cannot"
    if [ "$RESULT" == "$VERIFY" ]; then
        echo "Docker is not running. Stop script execution."
        exit 1 
    fi
}

function log_environment_configuration(){
    cd  $HOME_PATH

    export COMMIT_ID=$(git rev-parse HEAD)
    export LOG_FOLDER=$HOME_PATH/../metrics/output

    echo "************************************"
    echo "Save configurations in '$LOG_FOLDER'"
    echo "************************************"
     
    # service
    sed 's/\QA_API_KEY=.*/QA_API_KEY=/' "$HOME_PATH"/../../service/.env  > $LOG_FOLDER/tmp1-service.env
    sed 's/\MAAS_API_KEY=.*/MAAS_API_KEY=/' $LOG_FOLDER/tmp1-service.env  > $LOG_FOLDER/tmp2-service.env    
    sed '/^#/d;s/\DISCOVERY_API_KEY=.*/DISCOVERY_API_KEY=/' $LOG_FOLDER/tmp2-service.env > $LOG_FOLDER/tmp3-service.env
    sed '/^#/d;s/\PROXY_API_KEY=.*/PROXY_API_KEY=/' $LOG_FOLDER/tmp3-service.env > $LOG_FOLDER/${SESSION_ID}_service.env
    rm $LOG_FOLDER/tmp1-service.env
    rm $LOG_FOLDER/tmp2-service.env
    rm $LOG_FOLDER/tmp3-service.env

    # Save configs
    REPO_URL=$(git config --get remote.origin.url)
    printf "commit-id=%s\nrepo-url=%s\n" $COMMIT_ID $REPO_URL > $LOG_FOLDER/${SESSION_ID}_code.md
}

#**********************************************************************************
# Execution
# *********************************************************************************

echo "************************************"
echo " Build and start containers with Docker compose " 
echo "- 'QA-Service'"
echo "- 'Experiment-runner'"
echo "- 'Maas-mock'"
echo "************************************"

check_docker
log_environment_configuration

# 1. set needed common environment
echo "Home path:    $HOME_PATH"
echo "Session ID:   $SESSION_ID"
"/bin/sh" "${HOME_PATH}"/env_profile_generate.sh > ~/.env_profile

# 2. load application environment configurations
source $(pwd)/../service/.env
source $(pwd)/../metrics/experiment-runner/.env

# 3. set qa service docker context
cd $HOME_PATH/../service
export QA_SERVICE_DOCKER_CONTEXT="$(pwd)"
cd $HOME_PATH

# 4. set experiment-runner docker context
cd $HOME_PATH/../metrics/experiment-runner
export EXPERIMENT_RUNNER_DOCKER_CONTEXT="$(pwd)"
cd $HOME_PATH

# 5. set maas-mock docker context
cd $HOME_PATH/../maas-mock
export MAAS_MOCK_DOCKER_CONTEXT="$(pwd)"
cd $HOME_PATH

# 6. set metrics output docker mountpoint
cd $HOME_PATH/../metrics/output
export OUTPUT_MOUNTPOINT="$(pwd)"
cd $HOME_PATH

# 7. set metrics input docker mountpoint
cd $HOME_PATH/../metrics/input
export INPUT_MOUNTPOINT="$(pwd)"
echo $INPUT_MOUNTPOINT
cd $HOME_PATH

docker compose version
echo "**************** BUILD ******************" 
docker compose -f ./docker_compose.yaml build
echo "**************** START ******************" 
docker compose -f ./docker_compose.yaml up # --detach

#CONTAINER=$(docker ps | grep experimentrunner | awk '{print $7;}')
#docker exec -it experimentrunner sh
#docker compose -f ./docker_compose.yaml stop