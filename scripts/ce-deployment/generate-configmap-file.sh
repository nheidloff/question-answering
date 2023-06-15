#!/bin/bash
cat <<EOF
ELASTIC_SEARCH_URL=${ELASTIC_SEARCH_URL:-''}
ELASTIC_SEARCH_INDEX=${ELASTIC_SEARCH_INDEX:-''}
ELASTIC_SEARCH_USER=${ELASTIC_SEARCH_USER:-''}
ELASTIC_SEARCH_PASSWORD=${ELASTIC_SEARCH_PASSWORD:-''}
DISCOVERY_URL=${DISCOVERY_URL:-''}
DISCOVERY_INSTANCE=${DISCOVERY_INSTANCE:-''}
DISCOVERY_PROJECT=${DISCOVERY_PROJECT:-''}
DISCOVERY_COLLECTION_ID=${DISCOVERY_COLLECTION_ID:-''}
PRIME_QA_URL=${PRIME_QA_URL:-''}
RERANKER_URL=${RERANKER_URL:-''}
MAAS_URL=${MAAS_URL:-''}
PROXY_URL=${PROXY_URL:-''}
EXPERIMENT_METRICS_SESSION=${SESSION_ID:-''}
EXPERIMENT_LLM_NAME=${EXPERIMENT_LLM_NAME:-''}
EXPERIMENT_LLM_MIN_NEW_TOKENS=${EXPERIMENT_LLM_MIN_NEW_TOKENS:-''}
EXPERIMENT_LLM_MAX_NEW_TOKENS=${EXPERIMENT_LLM_MAX_NEW_TOKENS:-''}
EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS=${EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS:-''}
EXPERIMENT_LLM_PROMPT=${EXPERIMENT_LLM_PROMPT:-''}
EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS=${EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS:-''}
EXPERIMENT_RERANKER_MODEL=${EXPERIMENT_RERANKER_MODEL:-''}
EXPERIMENT_RERANKER_ID=${EXPERIMENT_RERANKER_ID:-''}
EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS=${EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS:-''}
EXPERIMENT_DISCOVERY_CHARACTERS=${EXPERIMENT_DISCOVERY_CHARACTERS:-''}
EXPERIMENT_DISCOVERY_FIND_ANSWERS=${EXPERIMENT_DISCOVERY_FIND_ANSWERS:-''}
EXPERIMENT_METRICS_DIRECTORY=${EXPERIMENT_METRICS_DIRECTORY:-''}
QA_API_KEY=${QA_API_KEY:-''}
DISCOVERY_API_KEY=${DISCOVERY_API_KEY:-''}
MAAS_API_KEY=${MAAS_API_KEY:-''}
PROXY_API_KEY=${PROXY_API_KEY:-''}
RERANKER2=${RERANKER2:-''}
RERANKER_API_KEY=${RERANKER_API_KEY:-''}
EOF