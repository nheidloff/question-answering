# Experiment-runner

## 1. Used main libraries

* [Hugging Face](https://huggingface.co/spaces/evaluate-metric/sacrebleu)

## 2. Setup environment

```sh
python3.11 -m venv ex-runner-env-3.11
source ./ex-runner-env-3.11/bin/activate
python3 -m pip install --upgrade pip
pip install -r requirements.txt
pip install --upgrade --force-reinstall -r requirements.txt
```

or 

```sh
python3 -m pip install requests pandas datasets huggingface_hub fsspec aiohttp sacrebleu python-dotenv pyinstaller evaluate openpyxl nltk rouge_score Flask flask_restful
```

## 3. Create an `.env` file

```sh
cd YOUR_PATH/question-answering/metrics/experiment-runner
cat ./env_template > .env
```

[Link to the ./env_template file.](/.env_template)

## 4. Run `experiment-runner`

### 4.1. Execute as command line application

```sh
source ./ex-runner-env-3.11/bin/activate
python3 exp-runner.py
```

* The `experiment runner` ends an experiment, when the retries count is reached.

* Input files

    * The Python app uses the `input` folder as the source folder for the Excel file with the data input. The input file name is based on `input_folder_name` and `input_excel_filename` environment variables.

* Output files

    * **Excel:** It creates a file in the `outputs` folder `output/SESSION_ID_output_anwser.xlsx`. The output file name is based on `output_session_id` and `output_question_resp_anwser_excel` environment variables.
    
    * **Error Log:** It creates a file in the `output` folder `output/SESSION_ID_error.log`. The output file name is based on `output_session_id` and `output_error_log` environment variables.

### 2. Execute as local containers

* You need to install 'Docker desktop' application

1. Execute the bash automation [start-containers.sh](../../scripts/start-containers.sh)

The automation builds the needed containers locally and executes them inside a 'Docker compose network'.

The applications:

* QA - Service
* MaaS mao

```sh
cd question-answering/scripts
sh start-containers.sh
```









