#!/bin/bash

echo "************************************"
echo " Build and start containers with Docker compose " 
echo "- 'QA-Service'"
echo "- 'Experiment-runner'"
echo "- 'Maas-mock'"
echo "************************************"

function check_docker () {
    ERROR=$(docker ps 2>&1)
    RESULT=$(echo $ERROR | grep 'Cannot' | awk '{print $1;}')
    VERIFY="Cannot"
    if [ "$RESULT" == "$VERIFY" ]; then
        echo "Docker is not running. Stop script execution."
        exit 1 
    fi
}

check_docker

# 1. set needed common environment
export HOME_PATH=$(pwd)
export SESSION_ID=$(date +%s)
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