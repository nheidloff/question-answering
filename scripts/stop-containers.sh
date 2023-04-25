#!/bin/bash

# ***** 1. Source global variables ****
source ~/.env_profile

echo "************************************"
echo " Terminate containers with Docker compose " 
echo "- 'QA-Service'"
echo "- 'Experiment-runner'"
echo "- 'Maas-mock'"
echo "************************************"

# ***** 2. Load application environment configurations
source $(pwd)/../service/.env
source $(pwd)/../metrics/experiment-runner/.env

# ***** 2. Stop compose
docker compose version
echo "**************** Terminate ******************" 
docker compose -f ./docker_compose.yaml stop
