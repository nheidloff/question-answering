#!/bin/bash

export HOME_PATH=$(pwd)
export SESSION_ID=$(date +%s)

echo "************************************"
echo "Environment configuration"
echo "************************************"
echo "- HOME_PATH :          ${HOME_PATH}"
echo "- SESSION_ID:          ${SESSION_ID}"

echo "************************************"
echo "Environment configuration save in '~/.env_profile'"
echo "************************************"
"/bin/sh" "${HOME_PATH}"/generate_env_profile.sh > ~/.env_profile
# cat ~/.env_profile

# bash scripts
export exp_runner="${HOME_PATH}/exp_runner_local.sh"
export qa_service="${HOME_PATH}/qa_local.sh"

echo "************************************"
echo "- Enable bash automation for execution"
echo "************************************"
chmod 755 ${exp_runner}
chmod 755 ${qa_service}

echo "************************************"
echo "- Open terminals"
echo "************************************"
open -a Terminal ${qa_service}
sleep 10
open -a Terminal ${exp_runner}