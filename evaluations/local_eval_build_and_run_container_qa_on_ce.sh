#!/bin/bash

source ./.env

export version="v0.0.1"
export image_name="evaluation-local"

mkdir $(pwd)/outputs
mkdir $(pwd)/inputs

echo "***** BUILD evaluation container ******"
docker build -t $image_name:$version .

echo "***** START evaluation container ******"
docker run --name="evaluation-run" -it --rm \
                -v $(pwd)/outputs:/app/outputs \
                -v $(pwd)/inputs:/app/inputs \
                -e endpoint="$endpoint" \
                -e api_url="$api_url" \
                -e username="$username" \
                -e password="$password" \
                -e verify_answer="$verify_answer" \
                -e input_excel_filename="$input_excel_filename" \
                -e input_folder_name="$input_folder_name" \
                -e output_question_resp_anwser_excel="$output_question_resp_anwser_excel" \
                -e output_question_resp_anwser="$output_question_resp_anwser" \
                -e output_error_log="$output_error_log" \
                -e output_session_id="$output_session_id" \
                -e output_folder_name="$output_folder_name" \
                -e export number_of_retrys="$export number_of_retrys" \
                $image_name:$version
