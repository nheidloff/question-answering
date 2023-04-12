#!/bin/bash
########################################
# Create a file based on the environment variables
# given by the dockerc run -e parameter
########################################
cat <<EOF
# Microservice endpoint and extraction
export endpoint="${endpoint}"
export api_url="${api_url}"
export username="${username}"
export password="${password}"
export verify_answer="${verify_answer}"
# inputs
export input_excel_filename="${input_excel_filename}"
export input_folder_name="${input_folder_name}"
export input_folder_name_qa_service_log="${input_folder_name_qa_service_log}"
# outputs
export output_question_resp_anwser_excel="${output_question_resp_anwser_excel}"
export output_question_resp_anwser="${output_question_resp_anwser}"
export output_error_log="${output_error_log}"
export output_session_id="${output_session_id}"
export output_folder_name="${output_folder_name}"
export number_of_retrys="${number_of_retrys}"
export container_run="${container_run}"
# NOT NEEDED INSIDE EVALUATE APPLICATION
# export host_ip="${number_of_retrys}"
EOF