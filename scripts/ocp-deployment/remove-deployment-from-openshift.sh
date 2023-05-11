#!/bin/bash

# **************** Global variables
export HOME_PATH=$(pwd)

# OPC - variables
source "$HOME_PATH"/.env
# IBM Cloud - variables
source "$HOME_PATH"/../.env

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
    export MASTER_NODE=$(echo $MASTER_NODE_TEMP | sed 's~http[s]*://~~g' | sed 's/\"//g')
    echo "Master node: $MASTER_NODE"

    open https://iam.cloud.ibm.com/identity/passcode

    echo "Insert passcode: "
    read login_passcode

    oc login -u passcode -p "${login_passcode}" --server=https://${MASTER_NODE}
}

function uninstall_helm_chart () {

    echo ""
    echo "*********************"
    echo "uninstall Helm chart"
    echo "*********************"
    echo ""

    cd $HOME_PATH/charts
    TEMP_PATH_EXECUTION=$(pwd)
    helm uninstall question-answering-helm
    
    cd $HOME_PATH
}

#**********************************************************************************
# Execution
# *********************************************************************************

login_to_ibm_cloud
connect_to_cluster
login_to_cluster
uninstall_helm_chart

