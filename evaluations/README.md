# Evaluations

## Used main libraries

* [Hugging Face](https://huggingface.co/spaces/evaluate-metric/sacrebleu)

## Setup environment

```sh
python3 -m pip install requests pandas datasets huggingface_hub fsspec aiohttp csv sacrebleu python-dotenv pyinstaller evaluate openpyxl absl nltk rouge_score
```

## Create an `.env` file

```sh
cd YOUR_PATH/question-answering/evaluations
cat ./env_template > .env
```

[Link to the ./env_template file.](/evaluations/.env_template)

## Run application

### 1. Execute as command line application

```sh
python3 evaluate.py
```

* The `evaluate app` ends an experiment, with the retry count being reached.

* Input files

    * The Python app uses the `inputs` folder as the source folder for the Excel file with the data input. The input file name is based on `input_folder_name` and `input_excel_filename` environment variables.

* Output files

    * **CSV:** It creates a file in the `outputs` folder `outputs/SESSION_ID_2023-03-31_output_anwser.csv`. The output file name is based on `output_session_id` and `output_question_resp_anwser` environment variables. 

    * **Excel:** It creates a file in the `outputs` folder `outputs/SESSION_ID_2023-03-31_output_anwser.xlsx`. The output file name is based on `output_session_id` and `output_question_resp_anwser_excel` environment variables.
    
    * **Error Log:** It creates a file in the `outputs` folder `outputs/2023-03-31_SESSION_ID_error.log`. The output file name is based on `output_session_id` and `output_error_log` environment variables.

### 2. Execute as a local container

#### 2.1 Build and run as a local container and the `question answering service` **doesn't run** on the same machine local machine as a container

```sh
sh qa_ce_and_eval_local_build_and_run_container.sh
```

* Example output:

```sh
...
******* invoke REST API ********

--- Request0 ---
Status  : 200
Question: My question?
Answer  : The answer.
--- Request1 ---
Status  : 200
Question: My question?
Answer  : The answer.
--- Request2 ---
...
```

#### 2.2 Build and run as a local container and the `question answering service` **does run** on the same machine local machine as a container

1. In this situation you need to get the local host IP address and save it, to use the address later 

```sh
ifconfig | grep 192.
```

Example output:

You should see your local host IP address.
In our example below the value is the IP `192.168.178.36`.

Insert the IP address in your `.env` file:

```sh
# Only needed when you run 
# question answering microservice and evaluate container
# on the same local machine.
export host_ip="192.168.178.36"
export container_run=True
```

2. Execute the bash script in a new terminal session.

```sh
sh qa_local_and_eval_local_build_and_run_container.sh
```





