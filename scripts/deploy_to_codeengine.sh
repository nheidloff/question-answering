#!/bin/bash

# **************** Global variables
# IBM Cloud - variables
source ./.env
# QA Service - variables
source ../service/.env

# Optional to change
export CE_CR_ACCESS_NAME=$CR
export CE_CR_SERVER_NAME=$CR

export CE_APP_IMAGE_URL=""
export CE_APP_CPU_CONFIG=1
export CE_APP_MEMORY_CONFIG=4G
export CE_APP_MAX_SCALE=10
export CE_APP_MIN_SCALE=1
export CE_APP_PORT=8080
export CE_PROJECT_NAMESPACE=""

export COMMIT_ID=""
export QA_DOCKERFILE_NAME="Dockerfile.jvm"

# **********************************************************************************
# Functions definition
# **********************************************************************************

function login_to_ibm_cloud () {
    
    echo ""
    echo "*********************"
    echo "loginIBMCloud"
    echo "*********************"
    echo ""

    ibmcloud login --apikey $IC_API_KEY
    ibmcloud target -r $IC_REGION
    ibmcloud target -g $IC_RESOURCE_GROUP
}

function setup_ce_project() {
  echo "**********************************"
  echo " Using following project: $CE_PROJECT_NAME" 
  echo "**********************************"

  ibmcloud ce project create --name $CE_PROJECT_NAME 
  ibmcloud ce project select -n $CE_PROJECT_NAME
  
  #to use the kubectl commands
  ibmcloud ce project select -n $CE_PROJECT_NAME --kubecfg
  
  CE_PROJECT_NAMESPACE=$(ibmcloud ce project get --name $CE_PROJECT_NAME --output json | grep "namespace" | awk '{print $2;}' | sed 's/"//g' | sed 's/,//g')
  echo "Code Engine project namespace: $CE_PROJECT_NAMESPACE"
  kubectl get pods -n $CE_PROJECT_NAMESPACE
}

function build_and_push_container () {

    export CI_TAG=$(git rev-parse HEAD)
    export CE_APP_IMAGE_URL="$CR/$CR_REPOSITORY/$CI_NAME:$CI_TAG"

    echo "****** BUILD *********"
    tmp_home=$(pwd)
    cd $(pwd)/../service
    docker build -f "$QA_DOCKERFILE_NAME" -t "$CE_APP_IMAGE_URL" .
    cd $tmp_home

    ibmcloud cr login
    docker push "$CE_APP_IMAGE_URL"

}

function setup_ce_container_registry_access() {
   
   ibmcloud ce registry create --name $CE_CR_ACCESS_NAME \
                               --server $CE_CR_SERVER_NAME \
                               --username $CE_CR_USERNAME \
                               --password $CE_CR_PASSWORD \
                               --email $CE_CR_EMAIL
}

function deploy_ce_application(){
   
    # Valid vCPU and memory combinations: https://cloud.ibm.com/docs/codeengine?topic=codeengine-mem-cpu-combo
    ibmcloud ce application create --name "$CE_APP_NAME" \
                                   --image "$CE_APP_IMAGE_URL" \
                                   --cpu "$CE_APP_CPU_CONFIG" \
                                   --memory "$CE_APP_MEMORY_CONFIG" \
                                   --registry-secret "$CE_CR_ACCESS_NAME" \
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
                                   --max-scale $CE_APP_MAX_SCALE \
                                   --min-scale $CE_APP_MIN_SCALE \
                                   --port $CE_APP_PORT 

    ibmcloud ce application get --name "$CE_APP_NAME"
    CE_APP_NAME_URL=$(ibmcloud ce application get --name "$CE_APP_NAME" -o url)
    echo "************************************"
    echo "Access the application $CE_APP_NAME - URL: $CE_APP_NAME_URL/q/openapi"
    echo "************************************"
}

# **** Kubernetes CLI ****

function kube_information(){

    echo "************************************"
    echo " Kubernetes info '$CE_APP_NAME': pods, deployments and configmaps details "
    echo "************************************"
    
    kubectl get pods -n $CE_PROJECT_NAMESPACE
    kubectl get deployments -n $CE_PROJECT_NAMESPACE
    kubectl get configmaps -n $CE_PROJECT_NAMESPACE

}

function kube_pod_log(){

    echo "************************************"
    echo " Kubernetes $CE_APP_NAME: log"
    echo "************************************"

    FIND=$CE_APP_NAME
    APP_POD=$(kubectl get pod -n $CE_PROJECT_NAMESPACE | grep $FIND | awk '{print $1}')
    echo "************************************"
    echo "Show log for the pod: $APP_POD"
    echo "************************************"
    kubectl logs $APP_POD
}

#**********************************************************************************
# Execution
# *********************************************************************************

login_to_ibm_cloud
setup_ce_project
setup_ce_container_registry_access
deploy_ce_application
kube_information
kube_pod_log