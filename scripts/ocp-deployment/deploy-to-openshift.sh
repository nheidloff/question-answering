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
    export MASTER_NODE=$(echo $MASTER_NODE_TEMP | sed 's~http[s]*://~~g' | sed 's/\"//g')
    echo "Master node: $MASTER_NODE"

    open https://iam.cloud.ibm.com/identity/passcode

    echo "Insert passcode: "
    read login_passcode

    oc login -u passcode -p "${login_passcode}" --server=https://${MASTER_NODE}
}

function install_helm_chart () {

    echo ""
    echo "*********************"
    echo "install Helm chart ./question-answering-helm/"
    echo "*********************"
    echo ""

    "/bin/sh" ./generate-values-file.sh > ./charts/question-answering-helm/values.yaml

    cd $HOME_PATH/charts
    TEMP_PATH_EXECUTION=$(pwd)
    helm dependency update ./question-answering-helm/
    helm install --dry-run --debug question-answering-helm ./question-answering-helm/
    echo "Verify the try run. Move on Y/N?"
    
    read INPUT_KEY

    if [[ "${INPUT_KEY}" == "Y" ]]; then
        echo "Moving on with the installation."
        helm install question-answering-helm ./question-answering-helm/
    else
        echo "Based on your input ($INPUT_KEY) the script ends here."
        exit 1
    fi
    
    cd $HOME_PATH
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
    docker push "$IMAGE_URL"
    
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
#uninstall_helm_chart

