# evaluations

## Used main libraries

* [Hugging Face](https://huggingface.co/spaces/evaluate-metric/sacrebleu)

## Setup environment

```sh
python3 -m pip install requests
python3 -m pip install pandas
python3 -m pip install datasets
python3 -m pip install huggingface_hub 
python3 -m pip install fsspec 
python3 -m pip install aiohttp
python3 -m pip install csv
python3 -m pip install sacrebleu
python3 -m pip install python-dotenv
python3 -m pip install pyinstaller
python3 -m pip install evaluate
python3 -m pip install openpyxl
python3 -m pip install absl
python3 -m pip install nltk
python3 -m pip install rouge_score
pip3 freeze > requirements.txt
```

* Use to install from the requirements file

```sh
pip install -r requirements.txt
```

## Create an `.env` file

```sh
cat ./env_template > .env
```

```sh
endpoint = "/YOUR_VALUE"
api_url = "YOUR_VALUE"
username='apikey'
password='YOUR_VALUE'
verify_answer="Answer"
input_folder_name = "inputs"
input_excel_filename = "input_excel.xlsx"
output_question_resp_anwser = "output_question_resp_anwser.csv"
output_question_resp_anwser_excel = "output_question_resp_anwser_excel.xlsx"
output_error_log = "error.log"
output_session_id = "1680027XXX"
output_folder_name = "outputs"
```

## Create `/evaluations/outputs` and `/evaluations/inputs` folder

```sh
mkdir ./evaluations/outputs
mkdir ./evaluations/inputs
```

## Run application

### 1. Execute as command line application

```sh
python3 evaluate.py
```

* Input files

    * The python app uses the `inputs` folder as the source folder for the excel file with the data input. The input file name is based on `input_folder_name` and `input_excel_filename` environment variables.

* Output files

    * **CSV:** It creates a file in the `outputs` folder `outputs/1680027XXX_output_question_resp_anwser.csv`. The output file name is based on `output_session_id` and `output_question_resp_anwser` environment variables. 

    * **Excel:** It creates a file in the `outputs` folder `outputs/1680027XXX_output_question_resp_anwser_excel.xlsx`. The output file name is based on `output_session_id` and `output_question_resp_anwser_excel` environment variables.
    
    * **Error Log:** It creates a file in the `outputs` folder `outputs/1680027XXX_error.log`. The output file name is based on `output_session_id` and `output_error_log` environment variables.

### 2. Execute as container

#### 2.1 Build container

```sh
version="v0.0.1"
image_name="evaluate"
docker build -t $image_name:$version .
```

#### 2.2 Start container

```sh
version="v0.0.1"
image_name="evaluate"
docker run -it --rm -v $(pwd)/outputs:/app/outputs -v $(pwd)/inputs:/app/inputs $image_name:$version
```
