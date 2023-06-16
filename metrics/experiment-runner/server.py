#!/usr/bin/env python
# encoding: utf-8
import os
from dotenv import load_dotenv
load_dotenv()

from flask import Flask
from flask_restful import Resource, Api

app = Flask(__name__)
api = Api(app)

class GetEnv(Resource):
    def get(self):
        # Customm debug information
        print("INFO: Customm debug information")
        app_debug_channel = os.environ.get("app_debug_channel")
              
        # qa service
        print("INFO: qa service")
        endpoint = os.environ.get("endpoint")
        api_url = os.environ.get("api_url")
        username=os.environ.get("username")
        password=os.environ.get("password")
        verify_answer=os.environ.get("verify_answer")
        
        # input
        print("INFO: input")
        input_excel_filename = os.environ.get("input_excel_filename")
        input_folder_name = os.environ.get("input_folder_name")
        input_folder_name_qa_service_metrics = os.environ.get("input_folder_name_qa_service_metrics")
        container_run = os.environ.get("container_run")
        i_dont_know = os.environ.get("i_dont_know")

        # output
        print("INFO: output")
        output_question_resp_anwser_excel = os.environ.get("output_question_resp_anwser_excel")
        output_error_log = os.environ.get("output_error_log")
        output_session_id = os.environ.get("output_session_id")
        output_folder_name = os.environ.get("output_folder_name")
        number_of_retries = os.environ.get("number_of_retries")
        

        # return
        print("INFO: return")
        return_value = str(output_question_resp_anwser_excel) + ";" +  \
                       str(output_error_log) + ";" +  \
                       str(output_session_id) + ";" + \
                       str(output_folder_name) + ";" + \
                       str(number_of_retries) + ";" + \
                       str(input_excel_filename) + ";" + \
                       str(input_folder_name) + ";" + \
                       str(input_folder_name_qa_service_metrics) + ";" + \
                       str(container_run) + ";" + \
                       str(endpoint) + ";" + \
                       str(api_url) + ";" + \
                       str(username) + ";" + \
                       str(password) + ";" + \
                       str(verify_answer) + ";" + \
                       str(i_dont_know) + ";" + \
                       str(app_debug_channel)
               
        return return_value

class Alive(Resource):
    def get(self):
        return_value = "Yes, I am alive!"
        return return_value

api.add_resource(Alive, '/')
api.add_resource(GetEnv, '/get_env')

if __name__ == '__main__':
    app.run(debug=False, port=8084)