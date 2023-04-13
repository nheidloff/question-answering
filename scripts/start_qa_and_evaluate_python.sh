#!/bin/bash

export SET_HOME_PATH=$(pwd)
export SET_SESSION_ID=$(date +%s)
export SET_M_DIR_NAME=myrun
export SET_CONT_CONF=True

"/bin/sh" ./generate_env_container_start.sh > $SET_HOME_PATH/.env_container_start

echo "************************************"
echo "    Configuration"
echo "************************************"
echo "- HOME_PATH :          $SET_HOME_PATH"
echo "- SESSION_ID:          $SET_SESSION_ID"
echo "- CONT_CONF:           $SET_CONT_CONF"
echo "- M_DIR_NAME:          $SET_M_DIR_NAME"

# bash scripts
export local_qa_build_run_container="$SET_HOME_PATH/local_qa_build_run_container.sh"
export qa_local_and_eval_local_build_and_run_container="$SET_HOME_PATH/../evaluations/qa_local_and_eval_local_build_and_run_container.sh"

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

