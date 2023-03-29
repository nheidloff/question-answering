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
import evaluate as eval
import sacrebleu as scb
import logging
import openpyxl

# ******************************************
# Global variables
endpoint = os.environ.get("endpoint")
api_url = os.environ.get("api_url")
username=os.environ.get("username")
password=os.environ.get("password")
verify_answer=os.environ.get("verify_answer")
input_excel_filename = os.environ.get("input_excel_filename")
input_folder_name = os.environ.get("input_folder_name")
output_question_resp_anwser = os.environ.get("output_question_resp_anwser")
output_question_resp_anwser_excel = os.environ.get("output_question_resp_anwser_excel")
output_error_log = os.environ.get("output_error_log")
output_session_id = os.environ.get("output_session_id")
output_folder_name = os.environ.get("output_folder_name")

print("*************************")
print("Environment configuration:")
print(f"Endpoint: {endpoint}")
print(f"API URL: {api_url}")
print(f"Username: {username}")
print(f"Password: {password}")
print(f"Verify answer: {verify_answer}")
print(f"Input Excel: {input_excel_filename}")
print(f"Output CSV: {output_question_resp_anwser}")
print(f"Output Excel: {output_question_resp_anwser_excel}")
print(f"Sesssion ID output prefix: {output_session_id}")
print(f"Error log: {output_error_log}")
print(f"Input folder: {input_folder_name}")
print(f"Output folder: {output_folder_name}")

# ******************************************
# get os path information
def get_input_path():
        global input_folder_name
        directory = os.getcwd()
        directory = directory + "/" + input_folder_name
        return directory

def get_output_path():
        global output_folder_name
        directory = os.getcwd()
        directory = directory + "/" + output_folder_name
        return directory

# ******************************************
# Bleu prepare date functions 

def bleu_run(input_filename):
    header, rows = bleu_get_data(input_filename)
    header = list(header.keys())
    return header, rows

def bleu_get_data(input_filename):
    rows = list()
    header = ['question', 'response', 'golden_anwser']

    try:
        workbook = openpyxl.load_workbook(input_filename)
        ws = workbook['temp_data']
    except:
        print(f"Error: Could not open {input_filename}\n")
    
    for row in ws.iter_rows(values_only=True):
        rows.append(row)

    return bleu_from_list_to_dict(header), rows

def bleu_from_list_to_dict(header):
    indices = [i for i in range(0, len(header))]
    header = {k: v for k, v in zip(header, indices)}
    print ( header )
    return header

# ******************************************
# Define logging
logger = logging.getLogger(output_session_id + "_" + output_error_log)
logger.setLevel(logging.INFO)
file_handler = logging.FileHandler(get_output_path() + "/" + output_session_id + "_" + output_error_log)
file_handler.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
file_handler.setFormatter(formatter)
logger.addHandler(file_handler)

# Select the active worksheet
def create_output_workbook (workbook_name):
        workbook = openpyxl.Workbook()
        worksheet = workbook.create_sheet("temp_data")
        worksheet.title = "temp_data"

        worksheet['A1'] = 'question'
        worksheet['B1'] = 'answer'
        worksheet['C1'] = 'golden_anwser'

        # Add a header to the worksheet
        # header_text = 'Temp Response Values'
        # worksheet.header_footer.oddHeader.text = header_text

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
    
    # Remove line break in rows
    new_rows = []
    #i = 0
    for row in rows:
        golden_answer = row[8]
        question      = row[1]

        golden_answer = golden_answer.replace('\n', '')
        question = question.replace('\n', '')

        new_rows.append([question, golden_answer])

        #print(f"-----[{i}]-----\n * Question: {question}\n * Golden answer: {golden_answer}")
        #i = i + 1

    new_header = [ "question", "golden_answer"]
    
    return new_header, new_rows

# ******************************************
# load existing evaluate values from csv
def load_existing_eval_values(csv_filepath):
        file = open(csv_filepath)
        csvreader = csv.reader(file)
        header = []
        header = next(csvreader)
        print(f"{header}")
        answers = []
        golds = [[]]
        for row in csvreader:              
                answers.append(row[2])
                golds[0].append(row[3])
        file.close()
        return answers, golds

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
                                                answer_text_clean = answer_part.replace('\n', '')
                                                answer_text = answer_text + ' ' + answer_text_clean
                                else:
                                        answer_text = answer_text_list[0]
                                        answer_text = answer_text.replace('\n', '')

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
        input_data_exists = True

        output_directory = get_output_path()
        input_directory = get_input_path()
        workbook_name_file = output_directory + "/"  + output_session_id + "_" + output_question_resp_anwser_excel

        # 1. use an input file to get the answers from the qa microserice
        if (input_data_exists == False):

                        # 1.1 load data from input file 
                        print(f"******* prepare input file ********\n")    
                        excel_input_filepath = input_directory + "/" + input_excel_filename                       
                        header, rows = load_input_excel(excel_input_filepath) 
                        print(f"Input header: {header}\n")                    
                        input_len=len(rows)
                        print(f"Input len: {input_len}\n")
                        row = rows[0]
                        question = row[0]
                        print(f"Question: {question}\n")
                        golden_answer = row[1]
                        print(f"Golden answer: {golden_answer}\n")
                        
                        # 1.2. Prepare an output file for logging the execution results
                        csv_output_filepath = output_directory + "/"  + output_session_id + "_" +  output_question_resp_anwser
                        csvfile = open(csv_output_filepath,'w',encoding='utf-8')
                        csvfile_writer = csv.writer(csvfile, delimiter=',', quoting=csv.QUOTE_MINIMAL)
                        csv_line = ['count','question','answer','golden_answer']
                        stripped_line = [cell.strip() for cell in csv_line]
                        csvfile_writer.writerow(stripped_line)
                        test_results = []
                        
                        # 1.3. Prepare a temp output excel for logging the execution results
                        workbook = create_output_workbook(workbook_name_file)

                        # 1.4. invoke endpoint with questions and write the test results
                        i = 0
                        j = 0
                        print(f"******* invoke RESP API ********\n")
                        for row in rows:
                                very_golden_answer = row[1]
                                if (len(very_golden_answer) != 0):
                                        question = row[0]
                                        golden_answer = row[1]
                                        print(f"--- Request{i} ---")
                                        answer_text, answer_text_len, answer_list, verify = invoke_qa(question)
                                        if ((verify == True) and (len(row[1]) != 0)):
                                                # 1.3.1 build input for the model evaluation
                                                j = j + 1
                                                row_value = [j,question,answer_text,golden_answer]
                                                test_results.append(row_value)
                                                answers.append(answer_text)
                                                golds[0].append(golden_answer)

                                                # 1.3.2 add the values to the output csv file
                                                csv_line = [str(i),question,answer_text,golden_answer]
                                                stripped_line = [cell.strip() for cell in csv_line]
                                                csvfile_writer.writerow(stripped_line)
                                                
                                                # 1.3.3 add the values to the output temp excel file
                                                # set value for cell B2=2
                                                j+1
                                                workbook = openpyxl.load_workbook(workbook_name_file)
                                                worksheet = workbook['temp_data']
                                                worksheet.cell(row=(j+1), column=1).value = question
                                                worksheet.cell(row=(j+1), column=2).value = answer_text
                                                worksheet.cell(row=(j+1), column=3).value = golden_answer
                                                workbook.save(workbook_name_file)

                                        else:
                                                message = "Problem in data value: " + str(i) + "____" + "Question: " + question
                                                logger.error(message)
                                else:
                                        message = "Data value " + str(i) + " ____" + " 'Golden answer' is emtpy: " + row[1]
                                        logger.error(message)
                                i = i + 1
                        workbook.save(workbook_name_file)      
                        csvfile.close()                    
        
        # 2. Create eval output         
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

        print ('Bleu: ' + str(sacrebleu), 'RougeL: ' + str(rouge.mid.fmeasure))

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    args = parser.parse_args()
    main(args)