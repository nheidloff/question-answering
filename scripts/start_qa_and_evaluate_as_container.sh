#!/bin/bash

export HOME_PATH=$(pwd)
export SESSION_ID="$(date +%s)"
export CONTAINER_RUN="True"
export METRICS_FOLDER_NAME="myrun"

echo "************************************"
echo "    Configuration"
echo "************************************"
echo "- HOME_PATH :          $HOME_PATH"
echo "- SESSION_ID:          $SESSION_ID"
echo "- CONTAINER_RUN:       $CONTAINER_RUN"
echo "- METRICS_FOLDER_NAME: $METRICS_FOLDER_NAME"

# bash scripts
export local_qa_build_run_container="$HOME_PATH/local_qa_build_run_container.sh"
export qa_local_and_eval_local_build_and_run_container="$HOME_PATH/../evaluations/qa_local_and_eval_local_build_and_run_container.sh"

echo "************************************"
echo "    Enable bash automation for execution"
echo "************************************"
chmod 755 $local_qa_build_run_container
chmod 755 $qa_local_and_eval_local_build_and_run_container

echo "************************************"
echo "    Open Terminals"
echo "************************************"
open -a Terminal $local_qa_build_run_container
sleep 10
open -a Terminal $qa_local_and_eval_local_build_and_run_container

