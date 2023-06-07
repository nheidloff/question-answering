#!/bin/bash

# Restore and deploy by an existing commit ID
export ARG_KEYS=${1:-"no_keys"}

# **************** Global variables
export HOME_PATH=$(pwd)
export SESSION_ID=$(date +%s)
export COMMIT_ID=""
export LOG_FOLDER=""

# **********************************************************************************
# Functions definition
# **********************************************************************************

function check_parameters () {
    echo "Keys: $ARG_KEYS"
    if [ "$ARG_KEYS" == "no_keys" ]; then
        export USE_KEYS="false"
        echo "Don't use '.keys' configuration."
    else       
        export USE_KEYS="true"
        echo "Use '.keys' configuration."
        verify_key_configuration
    fi
}

function check_docker () {
    ERROR=$(docker ps 2>&1)
    RESULT=$(echo $ERROR | grep 'Cannot' | awk '{print $1;}')
    VERIFY="Cannot"
    if [ "$RESULT" == "$VERIFY" ]; then
        echo "Docker is not running. Stop script execution."
        exit 1 
    fi
}

function verify_key_configuration () {
    
    cd  $HOME_PATH
    KEYS_RESULT=$(ls -a | grep .keys | head -n 1)
    if [ "$KEYS_RESULT" != ".keys" ]; then
       echo "No '.keys' file exists."
       exit 1
    fi
    
    #ENV_RESULT=$(ls -a | grep .env | head -n 1)
}

function log_environment_configuration(){
    cd  $HOME_PATH

    export COMMIT_ID=$(git rev-parse HEAD)
    export LOG_FOLDER=$HOME_PATH/../metrics/output

    echo "************************************"
    echo "Save configurations in '$LOG_FOLDER'"
    echo "************************************"
     
    # service
    sed 's/\QA_API_KEY=.*/QA_API_KEY=/' "$HOME_PATH"/../service/.env  > $LOG_FOLDER/tmp1-service.env
    sed 's/\MAAS_API_KEY=.*/MAAS_API_KEY=/' $LOG_FOLDER/tmp1-service.env  > $LOG_FOLDER/tmp2-service.env
    sed 's/\ELASTIC_SEARCH_PASSWORD=.*/ELASTIC_SEARCH_PASSWORD=/' $LOG_FOLDER/tmp2-service.env > $LOG_FOLDER/tmp3-service.env
    sed '/^#/d;s/\DISCOVERY_API_KEY=.*/DISCOVERY_API_KEY=/' $LOG_FOLDER/tmp3-service.env > $LOG_FOLDER/tmp4-service.env
    sed '/^#/d;s/\PROXY_API_KEY=.*/PROXY_API_KEY=/' $LOG_FOLDER/tmp4-service.env > $LOG_FOLDER/${SESSION_ID}-service.env
    rm $LOG_FOLDER/tmp1-service.env
    rm $LOG_FOLDER/tmp2-service.env
    rm $LOG_FOLDER/tmp3-service.env
    rm $LOG_FOLDER/tmp4-service.env

    # experiment runner
    sed '/^#/d;s/\password=.*/password=/' "$HOME_PATH"/../metrics/experiment-runner/.env  > $LOG_FOLDER/${SESSION_ID}-experiment-runner.env
    
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
#echo "- 'Local Elasticsearch'"
echo "************************************"

check_docker
check_parameters
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

# 8. load keys
if [ "$USE_KEYS" == "true" ]; then
    source $HOME_PATH/.keys
fi


# 9. Start compose
docker compose version
echo "**************** BUILD ******************" 
docker compose -f ./docker_compose.yaml build
echo "**************** START ******************" 
docker compose -f ./docker_compose.yaml up # --detach

#CONTAINER=$(docker ps | grep experimentrunner | awk '{print $7;}')
#docker exec -it experimentrunner sh
#docker compose -f ./docker_compose.yaml stop