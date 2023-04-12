#!/bin/bash

# **************** Global variables
source ./.env

export version="v0.0.1"
export image_name="question-answering-local"
export dockerfile_path="$(pwd)/../service/src/main/docker/Dockerfile.jvm"
export name=question-answering-service

# temp set metrics problem with '../' in the question-answering service
tmp_home=$(pwd)
cd ..
project_path=$(pwd)
cd $tmp_home

echo "****** BUILD *********"
cd $(pwd)/../service
docker build -f $dockerfile_path -t $image_name:$version .
cd $tmp_home

echo "***** STOP and DELETE existing '$name' container ******"
docker container stop -f "$name"
docker container rm -f "$name"

echo "****** RUN '$name' container *********"
cd $(pwd)/../service
docker run -i --rm -p 8080:8080 --name $name \
  -e QA_API_KEY=${QA_API_KEY} \
  -e DISCOVERY_API_KEY=${DISCOVERY_API_KEY} \
  -e DISCOVERY_URL=${DISCOVERY_URL} \
  -e DISCOVERY_INSTANCE=${DISCOVERY_INSTANCE} \
  -e DISCOVERY_PROJECT=${DISCOVERY_PROJECT} \
  -e DISCOVERY_COLLECTION_ID=${DISCOVERY_COLLECTION_ID} \
  -e PRIME_QA_URL=${PRIME_QA_URL} \
  -e RERANKER_URL=${RERANKER_URL} \
  -e MAAS_URL=${MAAS_URL} \
  -e MAAS_API_KEY=${MAAS_API_KEY} \
  -e PROXY_URL=${PROXY_URL} \
  -e PROXY_API_KEY=${PROXY_API_KEY} \
  -e EXPERIMENT_LLM_NAME=${EXPERIMENT_LLM_NAME} \
  -e EXPERIMENT_LLM_MIN_NEW_TOKENS=${EXPERIMENT_LLM_MIN_NEW_TOKENS} \
  -e EXPERIMENT_LLM_MAX_NEW_TOKENS=${EXPERIMENT_LLM_MAX_NEW_TOKENS} \
  -e EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS=${EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS} \
  -e EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS=${EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS} \
  -e EXPERIMENT_RERANKER_MODEL=${EXPERIMENT_RERANKER_MODEL} \
  -e EXPERIMENT_RERANKER_ID=${EXPERIMENT_RERANKER_ID} \
  -e EXPERIMENT_METRICS_RUN=${EXPERIMENT_METRICS_RUN} \
  -e EXPERIMENT_METRICS_SESSION=${EXPERIMENT_METRICS_SESSION} \
  -e EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS=${EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS} \
  -v "${project_path}"/metrics/myrun:/deployments/metrics \
  $image_name:$version
cd $tmp_home