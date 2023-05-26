#!/bin/bash

# **************** Global variables
source ./../.env

export CODEENGINE_CR_ACCESS_NAME=$CR
export CODEENGINE_CR_SERVER_NAME=$CR

export CODEENGINE_APP_IMAGE_URL="$CR/$CR_REPOSITORY/$CI_NAME:$CI_TAG"
export CODEENGINE_APP_CPU_CONFIG=1
export CODEENGINE_APP_MEMORY_CONFIG=4G
export CODEENGINE_APP_MAX_SCALE=10
export CODEENGINE_APP_MIN_SCALE=1
export CODEENGINE_APP_PORT=8080
export CODEENGINE_PROJECT_NAMESPACE=""

# **********************************************************************************
# Functions definition
# **********************************************************************************

function login_to_ibm_cloud () {
    
    echo ""
    echo "*********************"
    echo "Log in to IBM Cloud"
    echo "*********************"
    echo ""

    ibmcloud login --apikey $IBM_CLOUD_API_KEY
    ibmcloud target -r $IBM_CLOUD_REGION
    ibmcloud target -g $IBM_CLOUD_RESOURCE_GROUP
}

function connect_ce_project() {
  echo "**********************************"
  echo " Using following project: $CODEENGINE_PROJECT_NAME" 
  echo "**********************************"

  # 1. Connect to IBM Cloud Code Engine project
  ibmcloud ce project select -n $CODEENGINE_PROJECT_NAME
  
  # 2. Get the kubecfg to connect to the related cluster
  ibmcloud ce project select -n $CODEENGINE_PROJECT_NAME --kubecfg

  # 3. Get the project namespace
  CODEENGINE_PROJECT_NAMESPACE=$(ibmcloud ce project get --name $CODEENGINE_PROJECT_NAME --output json | grep "namespace" | awk '{print $2;}' | sed 's/"//g' | sed 's/,//g')
  echo "Code Engine project namespace: $CODEENGINE_PROJECT_NAMESPACE"

  # 4. List all running pods
  kubectl get pods -n $CODEENGINE_PROJECT_NAMESPACE

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
    
    # 1. Find the pod for the application
    FIND=$CODEENGINE_APP_NAME
    APP_POD=$(kubectl get pod -n $CODEENGINE_PROJECT_NAMESPACE | grep $FIND | awk '{print $1}')
    echo "************************************"
    echo "Show log for the pod: $APP_POD"
    echo "************************************"
    # 2. Show the logs for this pod
    kubectl logs -f $APP_POD
}

#**********************************************************************************
# Execution
# *********************************************************************************

login_to_ibm_cloud
connect_ce_project
kube_information
kube_pod_log