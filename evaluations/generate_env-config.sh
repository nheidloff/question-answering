#!/bin/bash
########################################
# Create a file based on the environment variables
# given by the dockerc run -e parameter
########################################
cat <<EOF
# Microservice endpoint and extraction
endpoint = "${endpoint}"
api_url ="${api_url}"
username="${username}"
password="${password}"
verify_answer="${verify_answer}"

# inputs
input_excel_filename = "${input_excel_filename}"
input_folder_name = "${input_folder_name}"

# outputs
output_question_resp_anwser_excel = "${output_question_resp_anwser_excel}"
output_question_resp_anwser = "${output_question_resp_anwser}"
output_error_log = "${output_error_log}"
output_session_id = "${output_session_id}"
output_folder_name = "${output_folder_name }"
EOF