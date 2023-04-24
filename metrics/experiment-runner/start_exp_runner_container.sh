#!/bin/bash

source ~/.env_profile

echo "*************************"
echo "HOME_PATH: ${GLOBAL_HOME_PATH}"
echo "QA_SERVICE_API_URL: ${QA_SERVICE_API_URL}"
echo "*************************"

cd $GLOBAL_HOME_PATH/../metrics/experiment-runner

# experiment-runner environment variables
source ./.env

export version="v0.0.1"
export api_url=$QA_SERVICE_API_URL
export image_name="experiment-runner-local"
#export host_ip_addr=$host_ip

# temp set metrics problem with '../' in the question-answering service
tmp_home=$(pwd)
cd ..
project_path=$(pwd)
cd $tmp_home
export mountpath_metrics="${project_path}/${input_folder_name_qa_service_metrics}"

echo "Path: $mountpath_outputs"
echo "Path: $mountpath_inputs"

echo "***** BUILD experiment-runner container ******"
docker build -t $image_name:$version .

echo "***** STOP and DELETE existing experiment-runner container ******"
docker container stop -f "experiment-runner-run"
docker container rm -f "experiment-runner-run"

echo "***** START experiment-runner container ******"
docker run --name="experiment-runner-run" -it --rm \
                -v "${mountpath_outputs}":/app/outputs \
                -v "${mountpath_inputs}":/app/inputs \
                -v "${mountpath_metrics}":/app/metrics \
                -e endpoint="$endpoint" \
                -e api_url="$api_url" \
                -e username="$username" \
                -e password="$password" \
                -e verify_answer="$verify_answer" \
                -e input_excel_filename="$input_excel_filename" \
                -e input_folder_name="$input_folder_name" \
                -e output_question_resp_anwser_excel="$output_question_resp_anwser_excel" \
                -e output_error_log="$output_error_log" \
                -e output_session_id="${C_SESSION_ID}" \
                -e output_folder_name="$output_folder_name" \
                -e number_of_retries="$number_of_retries" \
                -e container_run="${container_run}" \
                -e qa_service_on_cloud='True' \
                $image_name:$version
