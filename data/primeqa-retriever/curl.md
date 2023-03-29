```
curl -X 'POST' "http://141.125.109.94:50059/ask" -H 'accept: application/json' \
-H 'Content-Type: application/json' \
-d '
{
    "question": "When and for how much did IBM acquire Red Hat?",
    "retriever": {
        "retriever_id": "WatsonDiscovery",
        "parameters": [
            {
                "parameter_id": "count",
                "value": "5"
            }
        ],
        "provenance": "Watson Discovery"
    },
    "collection": {
        "collection_id": "7801e618-8411-2538-0000-0186cbc7aed4"
    },
    "reader": {
        "reader_id": "ExtractiveReader",
        "parameters": [
            {
                "parameter_id": "model",
                "value": "PrimeQA/nq_tydi_sq1-reader-xlmr_large-20221110"
            },
            {
                "parameter_id": "max_num_answers",
                "value": "1"
            },
            {
                "parameter_id": "max_answer_length",
                "value": "50"
            }
        ],
        "provenance": "PrimeQA"
    }
}
'
```