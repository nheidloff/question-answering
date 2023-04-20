#!/bin/bash

echo "************************************"
echo " Terminate containers with Docker compose " 
echo "- 'QA-Service'"
echo "- 'Experiment-runner'"
echo "- 'Maas-mock'"
echo "************************************"

# 1. set needed common environment
source ~/.env_profile

# 2. load application environment configurations
source $(pwd)/../service/.env
source $(pwd)/../metrics/experiment-runner/.env

# 3. stop
docker compose version
echo "**************** Terminate ******************" 
docker compose -f ./docker_compose.yaml stop
