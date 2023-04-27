#!/bin/bash

# **************** Global variables
export HOME_PATH=$(pwd)

# IBM Cloud - variables
source "$HOME_PATH"/.env
# QA Service - variables
source "$HOME_PATH"/../service/.env

# Optional to change
export CODEENGINE_CR_ACCESS_NAME=$CR
export CODEENGINE_CR_SERVER_NAME=$CR

export CODEENGINE_APP_IMAGE_URL=""
export CODEENGINE_APP_CPU_CONFIG=1
export CODEENGINE_APP_MEMORY_CONFIG=4G
export CODEENGINE_APP_MAX_SCALE=10
export CODEENGINE_APP_MIN_SCALE=1
export CODEENGINE_APP_PORT=8080
export CODEENGINE_PROJECT_NAMESPACE=""

export COMMIT_ID=""
export QA_DOCKERFILE_NAME="Dockerfile.jvm"

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

function login_to_ibm_cloud () {
    
    echo ""
    echo "*********************"
    echo "loginIBMCloud"
    echo "*********************"
    echo ""

    ibmcloud login --apikey $IBM_CLOUD_API_KEY 
    ibmcloud target -r $IBM_CLOUD_REGION
    ibmcloud target -g $IBM_CLOUD_RESOURCE_GROUP
}

function setup_ce_project() {
  echo "**********************************"
  echo " Using following project: $CODEENGINE_PROJECT_NAME" 
  echo "**********************************"

  RESULT=$(ibmcloud ce project get --name $CODEENGINE_PROJECT_NAME | grep "Status" |  awk '{print $2;}')
  if [[ $RESULT == "active" ]]; then
        echo "*** The project $PROJECT_NAME exists."
        ibmcloud ce project select -n $CODEENGINE_PROJECT_NAME
  else
        ibmcloud ce project create --name $CODEENGINE_PROJECT_NAME 
        ibmcloud ce project select -n $CODEENGINE_PROJECT_NAME
  fi

  #to use the kubectl commands
  ibmcloud ce project select -n $CODEENGINE_PROJECT_NAME --kubecfg
  
  CODEENGINE_PROJECT_NAMESPACE=$(ibmcloud ce project get --name $CODEENGINE_PROJECT_NAME --output json | grep "namespace" | awk '{print $2;}' | sed 's/"//g' | sed 's/,//g')
  echo "Code Engine project namespace: $CODEENGINE_PROJECT_NAMESPACE"
  kubectl get pods -n $CODEENGINE_PROJECT_NAMESPACE
}

function build_and_push_container () {
  
    export COMMIT_ID=$(git rev-parse HEAD)
    export CI_TAG=$COMMIT_ID
    export CODEENGINE_APP_IMAGE_URL="$CR/$CR_REPOSITORY/$CI_NAME:$CI_TAG"
    echo "Name: $CODEENGINE_APP_IMAGE_URL"
    echo "****** BUILD *********"
    cd "$HOME_PATH"/../service
    docker build -f "$HOME_PATH"/../service/src/main/docker/"$QA_DOCKERFILE_NAME" -t "$CODEENGINE_APP_IMAGE_URL" .
    cd "$HOME_PATH"
    
    # Login to container with IBM Cloud registy  
    ibmcloud cr login
     
    ERROR=$(ibmcloud target -g $CR_RESOURCE_GROUP | grep FAILED | awk '{print $1;}' 2>&1)
    RESULT=$(echo $ERROR | grep 'FAILED' | awk '{print $1;}')
    VERIFY="FAILED"
    if [ "$RESULT" == "$VERIFY" ]; then
        echo "Can't set to resource group: ($CR_RESOURCE_GROUP) but I move on."
    fi

    ibmcloud cr region-set $CR_REGION

    # Create a new namespace, if the namespace doesn't exists
    CURR_CONTAINER_NAMESPACE=$(ibmcloud cr namespace-list -v | grep $CR_REPOSITORY | awk '{print $1;}')
    if [ "$CR_REPOSITORY" != "$CURR_CONTAINER_NAMESPACE" ]; then
        ibmcloud cr namespace-add $CR_REPOSITORY
    fi

    # Login to IBM Cloud registy with Docker
    docker login -u iamapikey -p $IBM_CLOUD_API_KEY $CR_REGION 
    docker push "$CODEENGINE_APP_IMAGE_URL"
    
    ibmcloud target -g $IBM_CLOUD_RESOURCE_GROUP

}

function setup_ce_container_registry_access() {

    RESULT=$(ibmcloud ce registry get --name $CODEENGINE_CR_ACCESS_NAME --output  jsonpath='{.metadata.name}')
    if [[ $RESULT == $CODEENGINE_CR_ACCESS_NAME ]]; then
        echo "*** The ce container registry $CODEENGINE_CR_ACCESS_NAME for the $CODEENGINE_PROJECT_NAME exists."
    else
        ibmcloud ce registry create --name $CODEENGINE_CR_ACCESS_NAME \
                               --server $CODEENGINE_CR_SERVER_NAME \
                               --username $CODEENGINE_CR_USERNAME \
                               --password $CODEENGINE_CR_PASSWORD \
                               --email $CODEENGINE_CR_EMAIL
    fi
}

function deploy_ce_application(){
   
    # Valid vCPU and memory combinations: https://cloud.ibm.com/docs/codeengine?topic=codeengine-mem-cpu-combo
    RESULT=$(ibmcloud ce application get --name "$CODEENGINE_APP_NAME" --output  jsonpath='{.metadata.name}')
    if [[ $RESULT == $CODEENGINE_APP_NAME ]]; then
        echo "*** The ce application $CODEENGINE_APP_NAME for the $CODEENGINE_PROJECT_NAME exists."
        echo "*** Delete application!"
        RESULT=$(ibmcloud ce application delete --name $CODEENGINE_APP_NAME --force)
        VERIFY=$(echo $RESULT | grep OK | awk -F" " '{print $NF}')
        if [[ $VERIFY != "OK" ]]; then
           echo "Error problem to delete the $CODEENGINE_APP_NAME application"
           echo "$RESULT"
           echo "The script stops here."
           exit 1
        fi
    fi

    ibmcloud ce application create --name "$CODEENGINE_APP_NAME" \
                                   --image "$CODEENGINE_APP_IMAGE_URL" \
                                   --cpu "$CODEENGINE_APP_CPU_CONFIG" \
                                   --memory "$CODEENGINE_APP_MEMORY_CONFIG" \
                                   --registry-secret "$CODEENGINE_CR_ACCESS_NAME" \
                                   --env QA_API_KEY="$QA_API_KEY" \
                                   --env MAAS_URL="$MAAS_URL" \
                                   --env MAAS_API_KEY="$MAAS_API_KEY" \
                                   --env PROXY_API_KEY="$PROXY_API_KEY" \
                                   --env DISCOVERY_API_KEY="$DISCOVERY_API_KEY" \
                                   --env DISCOVERY_URL="$DISCOVERY_URL" \
                                   --env DISCOVERY_INSTANCE="$DISCOVERY_INSTANCE" \
                                   --env DISCOVERY_PROJECT="$DISCOVERY_PROJECT" \
                                   --env DISCOVERY_COLLECTION_ID="$DISCOVERY_COLLECTION_ID" \
                                   --env PRIME_QA_URL="$PRIME_QA_URL" \
                                   --env RERANKER_URL="$RERANKER_URL" \
                                   --env PROXY_URL="$PROXY_URL" \
                                   --env EXPERIMENT_LLM_PROMPT="$EXPERIMENT_LLM_PROMPT" \
                                   --env EXPERIMENT_LLM_NAME="$EXPERIMENT_LLM_NAME" \
                                   --env EXPERIMENT_LLM_MIN_NEW_TOKENS="$EXPERIMENT_LLM_MIN_NEW_TOKENS" \
                                   --env EXPERIMENT_LLM_MAX_NEW_TOKENS="$EXPERIMENT_LLM_MAX_NEW_TOKENS" \
                                   --env EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS="$EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS" \
                                   --env EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS="$EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS" \
                                   --env EXPERIMENT_RERANKER_MODEL="$EXPERIMENT_RERANKER_MODEL" \
                                   --env EXPERIMENT_RERANKER_ID="$EXPERIMENT_RERANKER_ID" \
                                   --env EXPERIMENT_METRICS_SESSION="$EXPERIMENT_METRICS_SESSION" \
                                   --env EXPERIMENT_METRICS_DIRECTORY="$EXPERIMENT_METRICS_DIRECTORY" \
                                   --env MAX_RESULTS="$MAX_RESULTS" \
                                   --max-scale $CODEENGINE_APP_MAX_SCALE \
                                   --min-scale $CODEENGINE_APP_MIN_SCALE \
                                   --port $CODEENGINE_APP_PORT
    
    ibmcloud ce application get --name "$CODEENGINE_APP_NAME"
    export CODEENGINE_APP_NAME_URL=$(ibmcloud ce application get --name "$CODEENGINE_APP_NAME" -o url)
    echo "************************************"
    echo "Access the application $CODEENGINE_APP_NAME - URL: $CODEENGINE_APP_NAME_URL/q/openapi"
    echo "************************************"
}

# **** Kubernetes CLI ****

function kube_information(){

    echo "************************************"
    echo " Kubernetes info '$CODEENGINE_APP_NAME': pods, deployments and configmaps details "
    echo "************************************"
    
    kubectl get pods -n $CODEENGINE_PROJECT_NAMESPACE
    kubectl get deployments -n $CODEENGINE_PROJECT_NAMESPACE
    kubectl get configmaps -n $CODEENGINE_PROJECT_NAMESPACE

}

function kube_pod_log(){

    echo "************************************"
    echo " Kubernetes $CODEENGINE_APP_NAME: log"
    echo "************************************"

    FIND=$CODEENGINE_APP_NAME
    APP_POD=$(kubectl get pod -n $CODEENGINE_PROJECT_NAMESPACE | grep $FIND | awk '{print $1}')
    echo "************************************"
    echo "Show log for the pod: $APP_POD"
    echo "************************************"
    kubectl logs $APP_POD
}

# **** Logging *****

function log_deployment_configuration_all(){
    
    echo "************************************"
    echo "Save configurations in deployment-log/all"
    echo "************************************"
    cd  $HOME_PATH
    FOLDERNAME="$(date +%Y-%m-%d-%T)-git-$COMMIT_ID"
    mkdir $HOME_PATH/../deployment-log/all/$FOLDERNAME
    
    # remove all comments of the envirement configuration and save in all
    sed '/^#/d;s/\IBM_CLOUD_API_KEY=.*/IBM_CLOUD_API_KEY=/' $HOME_PATH/.env > $HOME_PATH/../deployment-log/all/$FOLDERNAME/ibm-cloud.env
    sed '/^#/d;s/\password=.*/password=/' $HOME_PATH/../metrics/experiment-runner/.env > $HOME_PATH/../deployment-log/all/$FOLDERNAME/experiment-runner.env

    sed 's/\QA_API_KEY=.*/QA_API_KEY=/' $HOME_PATH/../service/.env  > $HOME_PATH/../deployment-log/all/$FOLDERNAME/tmp-service.env 
    sed '/^#/d;s/\DISCOVERY_API_KEY=.*/DISCOVERY_API_KEY=/' $HOME_PATH/../deployment-log/all/$FOLDERNAME/tmp-service.env > $HOME_PATH/../deployment-log/all/$FOLDERNAME/service.env
    rm $HOME_PATH/../deployment-log/all/$FOLDERNAME/tmp-service.env

    # create new files
    REPO_URL=$(git config --get remote.origin.url)
    printf "commit-id=%s\nrepo-url=%s\n" $COMMIT_ID $REPO_URL > $HOME_PATH/../deployment-log/all/$FOLDERNAME/code.txt
    printf "query-url=%s\n" $CODEENGINE_APP_NAME_URL > $HOME_PATH/../deployment-log/all/$FOLDERNAME/deployment-info.txt

}

function log_deployment_configuration_last(){
    
    echo "************************************"
    echo "Save configurations in deployment-log/last"
    echo "************************************"
    cd  $HOME_PATH
    
    # remove all comments of the envirement configuration and save in all
    sed '/^#/d;s/\DISCOVERY_API_KEY=.*/DISCOVERY_API_KEY=/' $HOME_PATH/../service/.env > $HOME_PATH/../deployment-log/last/service.env
    sed '/^#/d;s/\IBM_CLOUD_API_KEY=.*/IBM_CLOUD_API_KEY=/' $HOME_PATH/.env > $HOME_PATH/../deployment-log/last/ibm-cloud.env
    sed '/^#/d;s/\password=.*/password=/' $HOME_PATH/../metrics/experiment-runner/.env > $HOME_PATH/../deployment-log/last/experiment-runner.env

    # create new files
    REPO_URL=$(git config --get remote.origin.url)
    printf "commit-id=%s\nrepo-url=%s\n" $COMMIT_ID $REPO_URL > $HOME_PATH/../deployment-log/all/$FOLDERNAME/code.txt
    printf "query-url=%s\n" $CODEENGINE_APP_NAME_URL > $HOME_PATH/../deployment-log/last/deployment-info.txt

}

function set_global_env () {
    # 1. set needed common environment
    export SESSION_ID=$(date +%s)
    export QA_SERVICE_API_URL=$CODEENGINE_APP_NAME_URL
    echo "Home path:    $HOME_PATH"
    echo "Session ID:   $SESSION_ID"
    echo "Code Engine URL: $QA_SERVICE_API_URL"
    "/bin/sh" "${HOME_PATH}"/env_profile_generate.sh > ~/.env_profile
}

#**********************************************************************************
# Execution
# *********************************************************************************

check_docker
login_to_ibm_cloud
build_and_push_container
setup_ce_project
setup_ce_container_registry_access
deploy_ce_application
kube_information
kube_pod_log
set_global_env
log_deployment_configuration_all
log_deployment_configuration_last