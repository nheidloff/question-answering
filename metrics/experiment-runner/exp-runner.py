#!/usr/bin/env python

# Imports
import requests
import json
import argparse
import os
import csv
from dotenv import load_dotenv
load_dotenv()
from datasets import load_metric
import sacrebleu
import logging
import openpyxl
from datetime import date

# ******************************************
# Global variables

# qa service
endpoint = os.environ.get("endpoint")
api_url = os.environ.get("api_url")
username=os.environ.get("username")
password=os.environ.get("password")
verify_answer=os.environ.get("verify_answer")

# input
input_excel_filename = os.environ.get("input_excel_filename")
input_folder_name = os.environ.get("input_folder_name")
input_folder_name_qa_service_metrics = os.environ.get("input_folder_name_qa_service_metrics")
container_run = os.environ.get("container_run")

# output
output_question_resp_anwser = os.environ.get("output_question_resp_anwser")
output_question_resp_anwser_excel = os.environ.get("output_question_resp_anwser_excel")
output_error_log = os.environ.get("output_error_log")
output_session_id = os.environ.get("output_session_id")
output_folder_name = os.environ.get("output_folder_name")
number_of_retries = os.environ.get("number_of_retries")

print("*************************")
print("Experiment-runner configuration:")
print("")
print(f"- Endpoint: {endpoint}")
print(f"- API URL: {api_url}")
print(f"- retries: {number_of_retries}")
print(f"- Username: {username}")
print(f"- Password: {password}")
print(f"- Verify answer: {verify_answer}\n")
print(f"- Input Excel: {input_excel_filename}")
print(f"- Input folder name: {input_folder_name}")
print(f"- Input folder name qa service: {input_folder_name_qa_service_metrics}\n")
print(f"- Output folder name: {output_folder_name}")
print(f"- Sesssion ID output prefix: {output_session_id}")
print(f"- Output Excel: {output_question_resp_anwser_excel}\n")
print(f"- Error log name: {output_error_log}")
print(f"- Container run: {container_run}")

# ******************************************
# get os path information
def get_input_path():
        global input_folder_name
        global container_run
        if (container_run == "False"):
                directory = os.getcwd()
                directory = directory + "/../" + input_folder_name
        else:
                directory = os.getcwd()
                directory = directory + "/" + input_folder_name
        return directory

def get_output_path():
        global output_folder_name
        global container_run
        if (container_run == "False"):
                directory = os.getcwd()
                directory = directory + "/../" + output_folder_name
        else:
                directory = os.getcwd()
                directory = directory + "/" + output_folder_name
        return directory

def get_input_qa_service_metrics_local_path():
        global input_folder_name_qa_service_metrics
        directory = os.getcwd()
        
        if input_folder_name_qa_service_metrics != "":
                new_directory = directory + "/../output/" + input_folder_name_qa_service_metrics
        else:
                new_directory = directory + "/../output"
                
        return new_directory

def get_input_qa_service_metrics_container_path():
        global input_folder_name_qa_service_metrics
        global container_run
        
        if (container_run == "False"):
                directory = os.getcwd()
                directory = directory + "/../output"
        else:
                directory = os.getcwd()
                new_directory = directory + "/output"
        
        return new_directory

# ******************************************
# Bleu prepare eval data functions 

def bleu_run(input_filename):
    header, rows = bleu_get_data(input_filename)
    header = list(header.keys())
    return header, rows

def bleu_get_data(input_filename):
    rows = list()
    header = ['question', 'response', 'golden_anwser']

    try:
        workbook = openpyxl.load_workbook(input_filename)
        ws = workbook['experiment_data']
    except:
        print(f"Error: Could not open {input_filename}\n")
    
    for row in ws.iter_rows(values_only=True):
        rows.append(row)

    return bleu_from_list_to_dict(header), rows

def bleu_from_list_to_dict(header):
    indices = [i for i in range(0, len(header))]
    header = {k: v for k, v in zip(header, indices)}
    # print ( header )
    return header

# ******************************************
# Define logging
logger = logging.getLogger(output_session_id + "-" + output_error_log)
logger.setLevel(logging.INFO)
file_handler = logging.FileHandler(get_output_path() + "/" + output_session_id + "-" + output_error_log)
file_handler.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
file_handler.setFormatter(formatter)
logger.addHandler(file_handler)

# Create a "experiment_data" worksheet
def create_output_workbook (workbook_name):
        workbook = openpyxl.Workbook()
        worksheet = workbook.create_sheet("experiment_data")
        worksheet_blue = workbook.create_sheet("experiment_bleu_result")
        if 'Sheet1' in  workbook.sheetnames:
                 workbook.remove( workbook['Sheet1'])
        if 'Sheet' in  workbook.sheetnames:
                 workbook.remove( workbook['Sheet'])
        
        worksheet.title = "experiment_data"
        worksheet_blue.title = "experiment_bleu_result"
        worksheet_blue['A1'] = 'bleu'
        worksheet_blue['B1'] = 'RougeL'

        worksheet['A1'] = 'question'
        worksheet['B1'] = 'answer'
        worksheet['C1'] = 'golden_anwser'
        worksheet['D1'] = 'passage_1'
        worksheet['E1'] = 'passage_1_id'
        worksheet['F1'] = 'passage_2'
        worksheet['G1'] = 'passage_2_id'
        worksheet['H1'] = 'passage_3'
        worksheet['I1'] = 'passage_3_id'
        worksheet['J1'] = 'answer_passage_1'
        worksheet['K1'] = 'answer_passage_1_id'
        worksheet['L1'] = 'answer_passage_2'
        worksheet['M1'] = 'answer_passage_2_id'
        worksheet['N1'] = 'answer_passage_3'
        worksheet['O1'] = 'answer_passage_3_id'

        # Save the workbook as a new Excel file
        workbook.save(workbook_name)
        return workbook

# ******************************************
# load input excel
def load_input_excel(excel_input):
    wb = openpyxl.load_workbook(excel_input)
    ws = wb.active
    
    rows = []
    for rdx, row in enumerate(ws.iter_rows(values_only=True)):
        if rdx:
            rows.append(list(row))
        else:
            header = row
    
    new_rows = []
    
    # Extract data
    for row in rows:
        
        question      = row[1]
        passage_1     = row[2]
        passage_1_id  = row[3]
        passage_2     = row[4]
        passage_2_id  = row[5]
        passage_3     = row[6]
        passage_3_id  = row[7]
        golden_answer = row[8]

        new_rows.append([question, golden_answer, passage_1, passage_1_id, passage_2, passage_2_id, passage_3, passage_3_id ])

    new_header = [ "question", "golden_answer", "passage_1", "passage_1_id", "passage_2", "passage_2_id", "passage_3", "passage_3_id"]
    
    return new_header, new_rows

# ******************************************
# load qa service metrics from csv file
def load_qa_service_metrics(csv_filepath):
        print(f"QA -service experiment file: {csv_filepath}")
        file = open(csv_filepath)
        csvreader = csv.reader(file)
        header = []
        header = next(csvreader)
        print(f"{header}")

        # Reranker extract
        i = 0
        for column in header:
                # print(f"Column: {column} : {i}")
                if str(column) == "RESULT_RERANKER_PASSAGE1":
                        ranker_p_1 = i
                if str(column) == "RESULT_RERANKER_PASSAGE2":
                        ranker_p_2 = i
                if str(column) == "RESULT_RERANKER_PASSAGE3":
                        ranker_p_3 = i
                if str(column) == "RESULT_RERANKER_PASSAGE1_ID":
                        ranker_p_1_id = i   
                if str(column) == "RESULT_RERANKER_PASSAGE2_ID":
                        ranker_p_2_id = i   
                if str(column) == "RESULT_RERANKER_PASSAGE3_ID":
                        ranker_p_3_id = i
                i = i + 1                         

        qa_service_metrics = []
        for row in csvreader: 
                values = [ str(row[ranker_p_1]) , str(row[ranker_p_1_id]) , str(row[ranker_p_2]) , str(row[ranker_p_2_id]) , str(row[ranker_p_3]), str(row[ranker_p_3_id]) ]
                # print(f"Values:\n {values}")          
                qa_service_metrics.append(values)
        file.close()
        return qa_service_metrics

# ******************************************
# Invoke the REST API endpoint 
# of the qa service in code engine
def invoke_qa(question):
        global endpoint
        global api_url
        global username
        global password
        global output_error_log
        
        request_url = api_url + endpoint
        question_obj = {"query": ""}
        question_obj["query"] = "text:" + question

        # 1. send request
        response = requests.post(request_url, auth=(username, password), json=question_obj)
        
        print(f"Status  : {response.status_code}")
        status_code = response.status_code
        
        if (status_code != 200):
                answer_text = ""
                answer_text_len = 0
                answer_text_list = []
                # Log errors for the status_code
                message = "Response code: " + str(status_code) + "____" + "Question object: " + json.dumps(question_obj)
                print(f"Error: {message}")
                logger.error(message)
                return answer_text, answer_text_len, answer_text_list, False
        else:
                response_json = response.json()
                # 2. extract the result
                if ( response_json['results'] != False):
                        results = response_json['results']
                        result = results[0]
                        
                        # 3. verify is it an answer
                        answer = result['document_id']
                        answer_text = ""
                        answer_text_list = []

                        if (answer == verify_answer):
                                answer_text_list = result['text']
                                answer_text_len = len(answer_text_list)
                                
                                if( len(answer_text_list)>1):
                                        for answer_part in answer_text_list:
                                                #answer_text_clean = answer_part.replace('\n', '')
                                                answer_text_clean = answer_part
                                                answer_text = answer_text + ' ' + answer_text_clean
                                else:
                                        answer_text = answer_text_list[0]
                                        #answer_text = answer_text.replace('\n', '')

                                print(f"Question: {question}")
                                print(f"Answer  : {answer_text}")
                                return answer_text, answer_text_len, answer_text_list, True
                        else:
                                answer_text = ""
                                answer_text_len = 0
                                answer_text_list = []
                                return answer_text, answer_text_len, answer_text_list, False

                else:
                        answer_text = ""
                        answer_text_len = 0
                        answer_text_list = []
                        return answer_text, answer_text_len,  answer_text_list, False

# ******************************************
# Execution
def main(args):

        # Does input data exist?
        # - False: Invoke the microservice
        # - True: Use an existing csv file
        input_data_exists = False
        
        # Temp list for creating output files
        golds = [[]]

        # End experiment, when not all requests can be processed!
        end_experiment = False 
        
        # Get date to be added to the file names
        today = str(date.today())

        # Set paths for input
        output_directory = get_output_path()
        print(f"- Output dir: {output_directory}")
        input_directory = get_input_path()
        print(f"- Input dir: {input_directory}")
        
        if (container_run == "False"):
                input_qa_directory = get_input_qa_service_metrics_local_path()
                print(f"- Input qa dir (local): {input_qa_directory}")
                qa_metrics_run_file = input_qa_directory + "/" + output_session_id + "-Runs.csv"
        else:
                input_qa_directory = get_input_qa_service_metrics_container_path()
                print(f"- Input qa dir (container): {input_qa_directory}")
                qa_metrics_run_file = input_qa_directory + "/" + output_session_id + "-Runs.csv"
   
        workbook_name_file = output_directory + "/"  + output_session_id + "-" + output_question_resp_anwser_excel

        # 1. use an input file to get the answers from the qa microserice
        if (input_data_exists == False):

                        # 1.1 load data from input file 
                        print(f"******* prepare input data from file {input_excel_filename} ********\n")    
                        excel_input_filepath = input_directory + "/" + input_excel_filename                       
                        header, rows = load_input_excel(excel_input_filepath) 
                        print(f"- Input header: {header}")                    
                        input_len=len(rows)
                        print(f"- Input len: {input_len}")
                        row = rows[0]
                        question = row[0]
                        print(f"- First question: {question}")
                        golden_answer = row[1]
                        print(f"- First golden answer: {golden_answer}")
                               
                        # 1.2. Prepare an output excel for logging the execution results
                        workbook = create_output_workbook(workbook_name_file)

                        # 1.3. invoke endpoint with questions and write the test results
                        i = 0
                        j = 0
                        print(f"******* invoke REST API ********\n")
                        for row in rows:
                                very_golden_answer = row[1]
                                if (len(very_golden_answer) != 0):
                                        question      = row[0]
                                        golden_answer = row[1]
                                        passage_1     = row[2]
                                        passage_1_id  = row[3]
                                        passage_2     = row[4]
                                        passage_2_id  = row[5]
                                        passage_3     = row[6]
                                        passage_3_id  = row[7]

                                        print(f"--- Request {i} ---")
                                        answer_text, answer_text_len, answer_list, verify = invoke_qa(question)
                                        
                                        # 1.4.1 Retry if the request didn't work
                                        if (verify != True):
                                                print(f"--- Retry the request {i} for {number_of_retries } times ---")
                                                retries = int(number_of_retries)
                                                retries_count = 0
                                                
                                                while (retries_count != retries):
                                                
                                                        print(f"Retry counter : {retries_count} ---")
                                                        answer_text, answer_text_len, answer_list, verify = invoke_qa(question)
                                                        retries_count = retries_count + 1

                                                        if (verify == True):
                                                                break
                                                        else:
                                                                if (retries_count == retries):
                                                                        message = "END EXPERIMENT! - Retry: " + str(retries_count) + " for request " + str(i) + " didn't work!"
                                                                        print(message)
                                                                        logger.error(message)
                                                                        end_experiment = True
                                                                        
                                                                        # 1.4.2 add the error to the output temp excel file
                                                                        # set value for cell B2=2
                                                                        j = j + 1
                                                                        workbook = openpyxl.load_workbook(workbook_name_file)
                                                                        worksheet = workbook['experiment_data']
                                                                        worksheet.cell(row=(j+1), column=1).value = "FAILED"
                                                                        worksheet.cell(row=(j+1), column=2).value = "FAILED"
                                                                        worksheet.cell(row=(j+1), column=3).value = "FAILED"
                                                                        workbook.save(workbook_name_file)
                                                                        break
                                                                else: 
                                                                        message = "Retry: " + str(retries_count) + " for request " + str(i) + " didn't work!"
                                                                        print(message)
                                                                        logger.error(message)

                                        # 1.4.3 Request work and a anwser contains content
                                        if ((verify == True) and (len(row[1]) != 0) and ( end_experiment == False)):

                                                # 1.4.4 add the values to the output temp excel file
                                                # set value for cell B2=2
                                                j = j + 1

                                                workbook = openpyxl.load_workbook(workbook_name_file)
                                                worksheet = workbook['experiment_data']
                                                worksheet.cell(row=(j+1), column=1).value = question
                                                worksheet.cell(row=(j+1), column=2).value = answer_text
                                                worksheet.cell(row=(j+1), column=3).value = golden_answer
                                                worksheet.cell(row=(j+1), column=4).value = passage_1
                                                worksheet.cell(row=(j+1), column=5).value = passage_1_id
                                                worksheet.cell(row=(j+1), column=6).value = passage_2
                                                worksheet.cell(row=(j+1), column=7).value = passage_2_id
                                                worksheet.cell(row=(j+1), column=8).value = passage_3
                                                worksheet.cell(row=(j+1), column=9).value = passage_3_id

                                                workbook.save(workbook_name_file)

                                        else:
                                                message = "Problem in data value: " + str(i) + "____" + "Question: " + question
                                                print(f"Error: {message}")
                                                logger.error(message)
                                else:
                                        message = "Data value " + str(i) + " ____" + " 'Golden answer' is emtpy: " + row[1]
                                        print(f"Error: {message}")
                                        logger.error(message)
                                i = i + 1
                        
                        workbook.save(workbook_name_file)                       
        
        if (end_experiment == False):

                  # 2. Create experiment-runner blue result output         
                  header, rows = bleu_run(workbook_name_file)
                  header = bleu_from_list_to_dict(header)
                  responses = [row[header['response']] for row in rows]
                  golds = [[row[header['golden_anwser']]] for row in rows]
                   
                  metric = load_metric("sacrebleu")
                  metric.add_batch(predictions=responses, references=golds)
                  sacrebleu = metric.compute()["score"]

                  metric = load_metric("rouge")
                  metric.add_batch(predictions=responses, references=golds)
                  rouge = metric.compute()["rougeL"]

                  # 3. add results from the qa service metrics

                  metrics_results = load_qa_service_metrics(qa_metrics_run_file)
                  print(f"metrics_results: {len(metrics_results)} \n")
                  workbook = openpyxl.load_workbook(workbook_name_file)
        
                  j = 1
                  for row in metrics_results:
                        worksheet = workbook['experiment_data']
                        worksheet.cell(row=(j+1), column=10).value = row[0]
                        worksheet.cell(row=(j+1), column=11).value = row[1]
                        worksheet.cell(row=(j+1), column=12).value = row[2]
                        worksheet.cell(row=(j+1), column=13).value = row[3]
                        worksheet.cell(row=(j+1), column=14).value = row[4]
                        worksheet.cell(row=(j+1), column=15).value = row[5]
                        j = j + 1
                  
                  worksheet = workbook['experiment_bleu_result']
                  worksheet.cell(row=(2), column=1).value = str(sacrebleu)
                  worksheet.cell(row=(2), column=2).value = str(rouge.mid.fmeasure)

                  workbook.save(workbook_name_file)

                  # 4. Show results
                
                  print (f"******* outputs for session: {output_session_id} ********")
                  print (f"Excel output file : {workbook_name_file}\n")
                  count = len(responses) - 1
                  print (f"******* Bleu result based on {count} responses ********")
                  print ('Bleu: ' + str(sacrebleu), 'RougeL: ' + str(rouge.mid.fmeasure))
        
        else:
                  print (f"******* Experiment failed *************")
                  print (f"******* outputs for failed session: {output_session_id} ********")
                  print (f"Excel output file : {workbook_name_file}\n")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    args = parser.parse_args()
    main(args)