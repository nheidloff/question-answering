#!/bin/bash

# **************** Global variables
export HOME_PATH=$(pwd)

# OPC - variables
source "$HOME_PATH"/.env
# IBM Cloud - variables
source "$HOME_PATH"/../.env
# QA Service - variables
source "$HOME_PATH"/../../service/.env

function check_docker () {
    
    echo ""
    echo "*********************"
    echo "Check Docker"
    echo "*********************"
    echo ""

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
    echo "Login to IBM Cloud"
    echo "*********************"
    echo ""

    ibmcloud login --apikey $IBM_CLOUD_API_KEY 
    ibmcloud target -r $IBM_CLOUD_REGION
    ibmcloud target -g $IBM_CLOUD_RESOURCE_GROUP
}

function connect_to_cluster () {

    echo ""
    echo "*********************"
    echo "Connect to Cluster $CLUSTER_NAME"
    echo "*********************"
    echo ""

    ibmcloud ks cluster config -c $CLUSTER_NAME
}

function login_to_cluster () {

    echo ""
    echo "*********************"
    echo "Login to cluster $CLUSTER_NAME"
    echo "*********************"
    echo ""
    
    export MASTER_NODE_TEMP=$(ibmcloud oc cluster config -c  $CLUSTER_NAME --admin --output json | jq '."clusters" | .[] | ."cluster" | ."server" ')
    
    echo "Master node: $MASTER_NODE_TEMP"
    remove_string=http://
    export MASTER_NODE=${MASTER_NODE_TEMP#"$remove_string"}
    echo "Master node: $MASTER_NODE"

    open https://iam.cloud.ibm.com/identity/passcode

    echo "Insert passcode: "
    read login_passcode
    ${string#"$prefix"}
    oc login -u passcode -p $login_passcode --server=${MASTER_NODE}
}

function install_helm_chart () {

    echo ""
    echo "*********************"
    echo "installHelmChart"
    echo "*********************"
    echo ""

    cd $HOME_PATH/charts
    TEMP_PATH_EXECUTION=$(pwd)
    
    helm dependency update ./question-answering-helm/
    helm install --dry-run \
         --debug helm-test \
         --set pullsecret.PULLSECRET=${PULL_SECRET}  \
         --set container_registry.CR_RESOURCE_GROUP=${CR_RESOURCE_GROUP}  \
         --set container_registry.CR_REGION=${CR_REGION}  \
         --set container_registry.CR=${CR}  \
         --set container_registry.CR_REPOSITORY=${CR_REPOSITORY}  \
         --set container_image.CI_NAME=${CI_NAME}  \
         --set container_image.CI_TAG=${CI_TAG}  \
         --set qa.QA_API_KEY=${QA_API_KEY}  \
         --set discovery.DISCOVERY_API_KEY=${DISCOVERY_API_KEY}  \
         --set discovery.DISCOVERY_URL=${DISCOVERY_URL}  \
         --set discovery.DISCOVERY_INSTANCE=${DISCOVERY_INSTANCE}  \
         --set discovery.DISCOVERY_PROJECT=${DISCOVERY_PROJECT}  \
         --set discovery.DISCOVERY_COLLECTION_ID=${DISCOVERY_COLLECTION_ID}  \
         --set prime_qa.PRIME_QA_URL=${PRIME_QA_URL}  \
         --set reranker.RERANKER_URL=${RERANKER_URL}  \
         --set maas.MAAS_URL=${MAAS_URL}  \
         --set maas.MAAS_API_KEY=${MAAS_API_KEY}  \
         --set proxy.PROXY_URL=${PROXY_URL}  \
         --set proxy.PROXY_API_KEY=${PROXY_API_KEY}  \
         --set experiment.EXPERIMENT_METRICS_SESSION=${EXPERIMENT_METRICS_SESSION}  \
         --set experiment.EXPERIMENT_LLM_NAME=${EXPERIMENT_LLM_NAME}  \
         --set experiment.EXPERIMENT_LLM_MIN_NEW_TOKENS=${EXPERIMENT_LLM_MIN_NEW_TOKENS}  \
         --set experiment.EXPERIMENT_LLM_MAX_NEW_TOKENS=${EXPERIMENT_LLM_MAX_NEW_TOKENS}  \
         --set experiment.EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS=${EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS}  \
         --set experiment.EXPERIMENT_LLM_PROMPT=${EXPERIMENT_LLM_PROMPT}  \
         --set experiment.EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS=${EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS}  \
         --set experiment.EXPERIMENT_RERANKER_MODEL=${EXPERIMENT_RERANKER_MODEL}  \
         --set experiment.EXPERIMENT_RERANKER_ID=${EXPERIMENT_RERANKER_ID}  \
         --set experiment.EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS=${EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS}  \
         --set experiment.EXPERIMENT_DISCOVERY_CHARACTERS=${EXPERIMENT_DISCOVERY_CHARACTERS}  \
         --set experiment.EXPERIMENT_DISCOVERY_FIND_ANSWERS=${EXPERIMENT_DISCOVERY_FIND_ANSWERS}  \
         --set experiment.EXPERIMENT_METRICS_DIRECTORY=${EXPERIMENT_METRICS_DIRECTORY }  \
         ./question-answering-helm/

    helm lint
    # helm install $HELM_RELEASE_NAME ./question-answering-helm
        
    cd $HOME_PATH
}

function build_and_push_container () {
  
    export IMAGE_URL="$CR/$CR_REPOSITORY/$CI_NAME:$CI_TAG"
    echo "Name: $IMAGE_URL"
    echo "****** BUILD *********"
    cd "$HOME_PATH"/../../service
    docker build -f "$HOME_PATH"/../../service/src/main/docker/"$QA_DOCKERFILE_NAME" -t "$IMAGE_URL" .
    cd "$HOME_PATH"
    
    # Login to container with IBM Cloud registy  
    ibmcloud cr login

    ERROR=$(ibmcloud target -g $CR_RESOURCE_GROUP 2>&1)
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

function create_custom_docker_config_file () {

    echo ""
    echo "*********************"
    echo "Create custom Docker ConfigFile"
    echo "*********************"
    echo ""

    sed "s+IBM_CLOUD_API_KEY+$IBM_CLOUD_API_KEY+g;s+CR+$CR+g" "$HOME_PATH/custom_config.json_template" > "$(pwd)/custom_config.json"
    export PULL_SECRET=$(base64 -i "$HOME_PATH/custom_config.json")
}

#**********************************************************************************
# Execution
# *********************************************************************************

check_docker
login_to_ibm_cloud
build_and_push_container
create_custom_docker_config_file
connect_to_cluster
login_to_cluster
install_helm_chart

