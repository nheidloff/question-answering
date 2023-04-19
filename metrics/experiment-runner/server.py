#!/usr/bin/env python
# encoding: utf-8
import json
import subprocess

from flask import Flask
from flask_restful import Resource, Api

app = Flask(__name__)
api = Api(app)

class Start(Resource):
    def get():
        # print(subprocess.check_output(['ls', '-l']))
        return_value = "Can't start anything!"
        return return_value

class Alive(Resource):
    def get():
        # print(subprocess.check_output(['ls', '-l']))
        return_value = "Yes, I am alive!"
        return return_value

api.add_resource(Alive, '/')
api.add_resource(Start, '/start')

if __name__ == '__main__':
    app.run(debug=True, port=8084)