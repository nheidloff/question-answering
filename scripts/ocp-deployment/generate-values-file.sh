#!/bin/bash
cat <<EOF
# Default values for chart.
# This is a YAML-formatted file.
# Declare variables to be passed into your templates.
#  EXPERIMENT_LLM_PROMPT: "${EXPERIMENT_LLM_PROMPT}"
pullsecret: 
  PULLSECRET: "${PULL_SECRET}"
container_registry:
  CR_RESOURCE_GROUP: "${CR_RESOURCE_GROUP}"
  CR_REGION: "${CR_REGION}"
  CR: "${CR}"
  CR_REPOSITORY: "${CR_REPOSITORY}"
container_image:
  CI_NAME: "${CI_NAME}"
  CI_TAG: "${CI_TAG}"
qa:
  QA_API_KEY: "${QA_API_KEY}"
discovery:
  DISCOVERY_API_KEY: "${DISCOVERY_API_KEY}"
  DISCOVERY_URL: "${DISCOVERY_URL}"
  DISCOVERY_INSTANCE: "${DISCOVERY_INSTANCE}"
  DISCOVERY_PROJECT: "${DISCOVERY_PROJECT}"
  DISCOVERY_COLLECTION_ID: "${DISCOVERY_COLLECTION_ID}"
prime_qa:
  PRIME_QA_URL: "${PRIME_QA_URL:-NO_VALUE}"
reranker:
  RERANKER_URL: "${RERANKER_URL}"
maas:
  MAAS_URL: "${MAAS_URL}"
  MAAS_API_KEY: "${MAAS_API_KEY}"
proxy:
  PROXY_URL: "${PROXY_URL:-NO_VALUE}"
  PROXY_API_KEY: "${PROXY_API_KEY:-NO_VALUE}"
experiment:
  EXPERIMENT_METRICS_SESSION: "${SESSION_ID:-NO_VALUE}"
  EXPERIMENT_LLM_NAME: "${EXPERIMENT_LLM_NAME}"
  EXPERIMENT_LLM_MIN_NEW_TOKENS: "${EXPERIMENT_LLM_MIN_NEW_TOKENS}"
  EXPERIMENT_LLM_MAX_NEW_TOKENS: "${EXPERIMENT_LLM_MAX_NEW_TOKENS}"
  EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS: "${EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS}"
  EXPERIMENT_LLM_PROMPT: "EXAMPLE-TEST"
  EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS: "${EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS}"
  EXPERIMENT_RERANKER_MODEL: "${EXPERIMENT_RERANKER_MODEL}"
  EXPERIMENT_RERANKER_ID: "${EXPERIMENT_RERANKER_ID}"
  EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS: "${EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS}"
  EXPERIMENT_DISCOVERY_CHARACTERS: "${EXPERIMENT_DISCOVERY_CHARACTERS}"
  EXPERIMENT_DISCOVERY_FIND_ANSWERS: "${EXPERIMENT_DISCOVERY_FIND_ANSWERS}"
  EXPERIMENT_METRICS_DIRECTORY: "/deployments/metrics"
EOF