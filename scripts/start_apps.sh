#!/bin/bash

echo "************************************"
echo "                 Start" 
echo "- 'QA-Service' as local Quarkus application"
echo "- 'Experiment-runner' as Python application"
echo "************************************"

export HOME_PATH=$(pwd)
export SESSION_ID=$(date +%s)
echo "************************************"
echo "Environment configuration"
echo "************************************"
echo "- HOME_PATH :          ${HOME_PATH}"
echo "- SESSION_ID:          ${SESSION_ID}"

# Environment configuration save in '~/.env_profile'"
"/bin/sh" "${HOME_PATH}"/generate_env_profile.sh > ~/.env_profile
# cat ~/.env_profile

# bash scripts
export exp_runner="${HOME_PATH}/exp_runner_local.sh"
export qa_service="${HOME_PATH}/qa_local.sh"

# Enable bash automation for execution"
chmod 755 ${exp_runner}
chmod 755 ${qa_service}

echo "************************************"
echo "    Open application terminals"
echo "************************************"
echo "- QA Service"
open -a Terminal ${qa_service}
sleep 10
echo "- Experiment runner"
open -a Terminal ${exp_runner}

