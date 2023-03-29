#!/bin/env python
# -*- coding: utf-8 -*-

import csv
import argparse
import json
import os
import re

def handle_args():
    usage='usage'
    parser=argparse.ArgumentParser(usage)
    parser.add_argument('--ground_truth_fn', '-g', required=True)
    parser.add_argument('--passages_fn', '-p', required=True)

    args=parser.parse_args()
    return args

def main():
    args=handle_args()

    documents_in_ground_truth = {}

    with open(args.ground_truth_fn, 'rt') as in_f:
        reader = csv.DictReader(in_f, delimiter=',', quotechar='\"')
        for r, row in enumerate(reader):
            for pos in ("1", "2", "3"):
                if row['loio ' + pos] != "":
                    documents_in_ground_truth[re.sub(r'^loio', '', row['loio ' + pos])] = row['passage ' + pos]

    documents_in_corpus = {}
    with open(args.passages_fn) as passages_file:
        print(f'# Reading {args.passages_fn}')
        records = json.load(passages_file)
        for rec in records:
            id = rec['chunckid']
            documents_in_corpus[re.sub(r'^[0-9]\.', '', id)] = ' '.join(rec['text']), rec['title']

    num_not_found = 0
    for id in documents_in_ground_truth.keys():
        if not id in documents_in_corpus:
            print(f'Not found: {id}')
            num_not_found += 1

    print(f'{num_not_found} ground truth documents not found in the corpus')

# do main
if __name__=='__main__':
   main()
