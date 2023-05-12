#!/bin/bash

# **************** Global variables
export HOME_PATH=$(pwd)

# OPC - variables
source "$HOME_PATH"/.env
# IBM Cloud - variables
source "$HOME_PATH"/../.env
# QA Service - variables
source "$HOME_PATH"/../../service/.env

# Internal global
export COMMIT_ID=""
export LOG_FOLDER=""
export ROUTE=""

# **********************************************************************************
# Functions definition
# **********************************************************************************


function check_docker () {
    cd  $HOME_PATH

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
    cd  $HOME_PATH

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
    cd  $HOME_PATH

    echo ""
    echo "*********************"
    echo "Connect to Cluster $CLUSTER_NAME"
    echo "*********************"
    echo ""

    ibmcloud ks cluster config -c $CLUSTER_NAME
}

function login_to_cluster () {
    cd  $HOME_PATH

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
    cd  $HOME_PATH

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

function wait_for_pod () {
    cd  $HOME_PATH

    echo ""
    echo "*********************"
    echo "Wait for pod"
    echo "*********************"
    echo ""
    i=0
    while :
        do
            FIND="question-answering"
            STATUS_CHECK=$(oc get pods -n question-answering | grep $FIND | awk '{print $1;}')
            echo "Status: $STATUS_CHECK"
            if [ "$STATUS_CHECK" != "" ]; then
                echo "$(date +'%F %H:%M:%S') Status: pod found."
                STATUS_CHECK=$(oc get pods -n question-answering | grep $FIND | awk '{print $3;}')
                if [ "$STATUS_CHECK" == "Running" ]; then
                   echo "Pod is running"
                   oc get pods -n question-answering
                fi
                STATUS_CHECK=$(oc get pods -n question-answering | grep $FIND | awk '{print $2;}')
                if [ "$STATUS_CHECK" == "1/1" ]; then
                   echo "Pod is ready"
                   oc get pods -n question-answering
                   break
                fi

            else 
                echo "$(date +'%F %H:%M:%S') Status: waiting"               
            fi

            if [[ "$i" -gt 3 ]]; then
                echo "Verify the pod manually! The script ends here."
                exit 1
            fi
            
            i=$((i+1))
            
            sleep 3
        done

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
    cd  $HOME_PATH

    export COMMIT_ID=$(git rev-parse HEAD)
    export CI_TAG=$COMMIT_ID
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
    cd  $HOME_PATH
    echo ""
    echo "*********************"
    echo "Create custom Docker ConfigFile"
    echo "*********************"
    echo ""

    sed "s+IBM_CLOUD_API_KEY+$IBM_CLOUD_API_KEY+g;s+CR+$CR+g" "$HOME_PATH/custom_config.json_template" > "$(pwd)/custom_config.json"
    export PULL_SECRET=$(base64 -i "$HOME_PATH/custom_config.json")
}

function verify_service () {
    cd  $HOME_PATH

    ROUTE_NAME=$(oc get routes -n question-answering | grep 'question-answering-route' | awk '{print $1;}')
    echo $ROUTE

    echo ""
    echo "***********************"
    echo "Wait for route"
    echo "***********************"
    echo ""
    i=0
    while :
        do
            FIND="question-answering-route"
            STATUS_CHECK=$(oc get routes -n question-answering | grep $FIND | awk '{print $1;}')
            echo "Status: $STATUS_CHECK"
            if [ "$STATUS_CHECK" = "" ]; then
                echo "$(date +'%F %H:%M:%S') Status: $FIND not found."
                echo "------------------------------------------------------------------------"
            fi

            if [ "$STATUS_CHECK" = "$FIND" ]; then
                echo "$(date +'%F %H:%M:%S') Status: $FIND is Ready"
                ROUTE=$(oc get routes -n question-answering | grep $FIND | awk '{print $2;}')
                echo "Route : $ROUTE"
                echo "------------------------------------------------------------------------"
                break
            fi

            if [[ "$i" -gt 3 ]]; then
                echo "Verify the route manually! The script ends here."
                exit 1
            fi

            i=$((i+1))
            sleep 3
        done
    
    QUERY="Do you know IBM?"
    
    curl -X POST -u "apikey:$QA_API_KEY" --header "Content-Type: application/json" --data "{ \"query\": \"text:$QUERY\" }" "$ROUTE/query"
}

function log_deployment_configuration(){
    
    echo "************************************"
    echo "Save configurations in `scripts/ocp-deployment/logs`"
    echo "************************************"
    cd  $HOME_PATH

    FOLDERNAME="$(date +%Y-%m-%d-%T)-git-$COMMIT_ID"
    export LOG_FOLDER=$HOME_PATH/logs/$FOLDERNAME
    mkdir $LOG_FOLDER
    
    # remove all comments of the envirement configuration and save in all
    
    sed '/^#/d;s/\IBM_CLOUD_API_KEY=.*/IBM_CLOUD_API_KEY=/' $HOME_PATH/../.env > $LOG_FOLDER/ibm-cloud.env
    sed '/^#/d;s/\IBM_CLOUD_API_KEY=.*/IBM_CLOUD_API_KEY=/' $HOME_PATH/.env > $LOG_FOLDER/ocp-deployment.env
    sed '/^#/d' $HOME_PATH/charts/question-answering-helm/values.yaml > $LOG_FOLDER/helm-values.yaml
    sed '/^#/d' $HOME_PATH/generate-values-file.sh > $LOG_FOLDER/helm-generate-values-file.sh
    
    # service
    sed 's/\QA_API_KEY=.*/QA_API_KEY=/' "$HOME_PATH"/../../service/.env  > $LOG_FOLDER/tmp1-service.env
    sed 's/\MAAS_API_KEY=.*/MAAS_API_KEY=/' $LOG_FOLDER/tmp1-service.env  > $LOG_FOLDER/tmp2-service.env    
    sed '/^#/d;s/\DISCOVERY_API_KEY=.*/DISCOVERY_API_KEY=/' $LOG_FOLDER/tmp2-service.env > $LOG_FOLDER/tmp3-service.env
    sed '/^#/d;s/\PROXY_API_KEY=.*/PROXY_API_KEY=/' $LOG_FOLDER/tmp3-service.env > $LOG_FOLDER/service.env
    rm $LOG_FOLDER/tmp1-service.env
    rm $LOG_FOLDER/tmp2-service.env
    rm $LOG_FOLDER/tmp3-service.env

    # create new files
    REPO_URL=$(git config --get remote.origin.url)
    printf "commit-id=%s\nrepo-url=%s\n" $COMMIT_ID $REPO_URL > $LOG_FOLDER/code.txt
    printf "query-url=%s\n" $ROUTE > $LOG_FOLDER/deployment-info.txt

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
wait_for_pod
verify_service
log_deployment_configuration
#uninstall_helm_chart

