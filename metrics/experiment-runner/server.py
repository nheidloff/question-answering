#!/usr/bin/env python
# encoding: utf-8
import json
import subprocess

from flask import Flask
from flask_restful import Resource, Api

app = Flask(__name__)
api = Api(app)

class Start(Resource):
    def start():
        # print(subprocess.check_output(['ls', '-l']))
        return json.dumps({"container":"start"})

class Alive(Resource):
    def alive():
        # print(subprocess.check_output(['ls', '-l']))
        return json.dumps({"container":"alive"})

api.add_resource(Alive, '/')
api.add_resource(Start, '/start')

if __name__ == '__main__':
    app.run(debug=True, port=8084)