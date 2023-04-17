import argparse
from datasets import load_metric
import openpyxl

#python3 -u blue.py --input_filename Book1.xlsx

def run(args):
    header, rows = get_data(args.input_filename)
    header = list(header.keys())
    return header, rows

def get_data(filename):
    rows = list()
    header = ['query', 'response', 'goldanswer']
    workbook = openpyxl.load_workbook(filename)
    ws = workbook.active
    
    for row in ws.iter_rows(values_only=True):
        rows.append(row)

    return from_list_to_dict(header), rows

def from_list_to_dict(header):
    indices = [i for i in range(0, len(header))]
    header = {k: v for k, v in zip(header, indices)}
    print ( header )
    return header

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input_filename", help="input filename of the data in xlsx format",
                        type=str)

    args = parser.parse_args()
    header, rows = run(args)

    header = from_list_to_dict(header)
    responses = [row[header['response']] for row in rows]
    golds = [[row[header['goldanswer']]] for row in rows]

    metric = load_metric("sacrebleu")
    metric.add_batch(predictions=responses, references=golds)
    sacrebleu = metric.compute()["score"]

    metric = load_metric("rouge")
    metric.add_batch(predictions=responses, references=golds)
    rouge = metric.compute()["rougeL"]

    print ('Bleu: ' + str(sacrebleu), 'RougeL: ' + str(rouge.mid.fmeasure))

if __name__ == "__main__":
    main()
