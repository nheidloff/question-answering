#!/bin/bash
cat <<EOF
# Configuration for QA services and experiment-runner
export GLOBAL_HOME_PATH=${HOME_PATH}
export GLOBAL_SESSION_ID=${SESSION_ID}
export GLOBAL_QA_SERVICE_URL=${QA_SERVICE_API_URL}
EOF