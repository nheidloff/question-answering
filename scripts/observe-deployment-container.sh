#!/bin/bash

# **************** Global variables
source ./.env

export CE_CR_ACCESS_NAME=$CR
export CE_CR_SERVER_NAME=$CR

export CE_APP_IMAGE_URL="$CR/$CR_REPOSITORY/$CI_NAME:$CI_TAG"
export CE_APP_CPU_CONFIG=1
export CE_APP_MEMORY_CONFIG=4G
export CE_APP_MAX_SCALE=10
export CE_APP_MIN_SCALE=1
export CE_APP_PORT=8080
export CE_PROJECT_NAMESPACE=""

# **********************************************************************************
# Functions definition
# **********************************************************************************

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
  echo " Using following project: $CE_PROJECT_NAME" 
  echo "**********************************"

  ibmcloud ce project select -n $CE_PROJECT_NAME
  
  #to use the kubectl commands
  ibmcloud ce project select -n $CE_PROJECT_NAME --kubecfg
  
  CE_PROJECT_NAMESPACE=$(ibmcloud ce project get --name $CE_PROJECT_NAME --output json | grep "namespace" | awk '{print $2;}' | sed 's/"//g' | sed 's/,//g')
  echo "Code Engine project namespace: $CE_PROJECT_NAMESPACE"
  kubectl get pods -n $CE_PROJECT_NAMESPACE
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
    kubectl logs -f $APP_POD
}

#**********************************************************************************
# Execution
# *********************************************************************************

login_to_ibm_cloud
setup_ce_project
kube_information
kube_pod_log