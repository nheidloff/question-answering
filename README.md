# Question Answering Service based on IBM Software

This repo contains a simple implementation of a `Question Answering` microservice which supports generative conversational search scenarios by leveraging `Foundation Models`, IBM software and open source.

At this point three steps are taken to generate answers:

1. Full text searches
2. Re-ranking
3. Answer generation

Two large `Large Language Models` are used:

1. Re-ranker which is typically an encoder-based transformer
2. Answer generator which is typically a decoder-based transformer

Involved technologies:

* IBM Watson Discovery
* ColBERT Reranker DrDecr in PrimeQA hosted on IBM Cloud
* FLAN-T5 hosted via some MaaS (Model as a Service) provider
* Optional: IBM Watson Assistant
* Optional: IBM Code Engine


## Screenshots of Sample Scenario

Sample documents have been uploaded to Watson Discovery and integrated in PrimeQA. The results of PrimeQA are passed as input to a prompt executed via a FLAN fine-tuned model.

The answer to the question "When and for how much did IBM acquire Red Hat?" is generated from two different documents.

<kbd><img src="screenshots/Demo3.png" /></kbd>

<kbd><img src="screenshots/Demo4.png" /></kbd>


## Endpoints

There are several endpoints to test and compare results. Main flows:

1. /query: Reads documents from Discovery, re-ranks results and uses MaaS to return answer
2. /query-discovery-maas: Returns answer from Discovery and MaaS
3. /query-primeqa-maas: Returns answer from PrimeQA (connected to Discovery) and MaaS

Further [endpoints](https://github.com/nheidloff/question-answering/blob/main/service/src/main/java/com/ibm/question_answering/AnswerResource.java) can be used for testing.

Flow 1: /query: Reads documents from Discovery, re-ranks results and uses MaaS to return answer

<kbd><img src="screenshots/qa-architecture-flow2.png" /></kbd>


## Run the Service

The service can be run locally via Docker or via Java and Maven. 

Environment variables are used for the [configuration](https://github.com/nheidloff/question-answering/blob/main/service/.env_template). 

```
cd servive
cp .env_template .env
code .env
```

After invoking REST APIs the results are also stored in the 'metrics' directory:

* [Last-Run.md](metrics/sample/Last-Run.md)
* [1679920979-Runs.csv](metrics/sample/1679920979-Runs.csv)
* [1679920979-Metadata.csv](metrics/sample/1679920979-Metadata.csv)

Via Java 17 and Maven 3.9:

```
cd servive
mvn packages
source .env
echo $EXPERIMENT_METRICS_SESSION
echo $QA_API_KEY
mvn quarkus:dev
```

Locally via Docker:

The following commands allow using /query-mock-confident and /query-primeqa. For the other endpoints additional environment variables need to be passed in.

```
cd service
docker build -f src/main/docker/Dockerfile.jvm -t question-answering:latest .
source .env
echo $EXPERIMENT_METRICS_SESSION
echo $QA_API_KEY
docker run -i --rm -p 8080:8080 \
  -e QA_API_KEY=${QA_API_KEY} \
  -e DISCOVERY_API_KEY=${DISCOVERY_API_KEY} \
  -e DISCOVERY_URL=${DISCOVERY_URL} \
  -e DISCOVERY_INSTANCE=${DISCOVERY_INSTANCE} \
  -e DISCOVERY_PROJECT=${DISCOVERY_PROJECT} \
  -e DISCOVERY_COLLECTION_ID=${DISCOVERY_COLLECTION_ID} \
  -e PRIME_QA_URL=${PRIME_QA_URL} \
  -e RERANKER_URL=${RERANKER_URL} \
  -e MAAS_URL=${MAAS_URL} \
  -e MAAS_API_KEY=${MAAS_API_KEY} \
  -e PROXY_URL=${PROXY_URL} \
  -e PROXY_API_KEY=${PROXY_API_KEY} \
  -e EXPERIMENT_METRICS_SESSION${EXPERIMENT_METRICS_RUN} \
  -e EXPERIMENT_LLM_NAME=${EXPERIMENT_LLM_NAME} \
  -e EXPERIMENT_LLM_MIN_NEW_TOKENS=${EXPERIMENT_LLM_MIN_NEW_TOKENS} \
  -e EXPERIMENT_LLM_MAX_NEW_TOKENS=${EXPERIMENT_LLM_MAX_NEW_TOKENS} \
  -e EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS=${EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS} \
  -e EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS=${EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS} \
  -e EXPERIMENT_RERANKER_MODEL=${EXPERIMENT_RERANKER_MODEL} \
  -e EXPERIMENT_RERANKER_ID=${EXPERIMENT_RERANKER_ID} \
  -e EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS={EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS} \
  -e EXPERIMENT_DISCOVERY_CHARACTERS={EXPERIMENT_DISCOVERY_CHARACTERS} \
  -e EXPERIMENT_DISCOVERY_FIND_ANSWERS={EXPERIMENT_DISCOVERY_FIND_ANSWERS} \
  -e EXPERIMENT_LLM_PROMPT={EXPERIMENT_LLM_PROMPT} \
  -v $(pwd)/../metrics:/deployments/metrics \
  question-answering:latest
```

Remotely on Code Engine:

```
export QA_API_KEY=xxx;
curl -v -X POST -u "apikey:$QA_API_KEY" --header "Content-Type: application/json" --data "{\"query\": \"text:When and for how much did IBM acquire Red Hat?\"}" "https://mock-api.xxx.us-east.codeengine.appdomain.cloud/query-mock-confident" | jq '.'
```

## Run Experiments

Experiments can be run locally via Python. 

Environment variables are used for the [configuration](https://github.com/nheidloff/question-answering/blob/main/evaluations/.env_template). 

Define $QA_API_KEY and $EXPERIMENT_METRICS_SESSION from previous step. Define the name and the location of your ground truth file 'input_excel_filename'.

```
cd evaluations
cp .env_template .env
code .env
```

Via Python 3.9:

```
cd evaluations
python3 -m pip install requests pandas datasets huggingface_hub fsspec aiohttp csv sacrebleu python-dotenv pyinstaller evaluate openpyxl absl nltk rouge_score
source .env
python3 evaluate.py
```

The evaluate script invokes all questions from the ground truth document.

As result of an experiment the 'Bleu' and 'Rouge' values are displayed. Additionally a spreadsheet with information from the ground truth file as well as the results is created in 'evaluations/outputs/'.


## Sample REST API Invocations

Sample query that returns one answer plus relevant documents:

```
curl -v -X POST -u "apikey:0123456789" --header "Content-Type: application/json" --data "{   \"query\": \"text:When and for how much did IBM acquire Red Hat?\" }" "http://localhost:8080/query-mock-confident" | jq '.'

{
  "matching_results": 2,
  "retrievalDetails": {
    "document_retrieval_strategy": "llm"
  },
  "results": [
    {
      "document_id": "472ec509-9861-45aa-8bce-983289032484",
      "title": "Answer",
      "text": [
        "IBM has acquired Red Hat for $34 billion in October 2018."
      ],
      "link": null,
      "document_passages": null
    },
    {
      "document_id": "086988c4-ab65-44f6-a2b5-5bfdd8afdb44",
      "title": "IBM acquires Red Hat",
      "text": [
        "It's official - IBM has acquired Red Hat! The deal was announced in October 2018. IBM Closes Landmark Acquisition of Red Hat."
      ],
      "link": "https://www.ibm.com/support/pages/ibm-acquires-red-hat",
      "document_passages": null
    },
    {
      "document_id": "fdc7a154-497b-4115-bb71-b3d20fe0c822",
      "title": "IBM Closes Landmark Acquisition of Red Hat; Defines Open, Hybrid Cloud Future",
      "text": [
        "IBM (NYSE:IBM) and Red Hat announced today that they have closed the transaction under which IBM acquired all of the issued and outstanding common shares of Red Hat for $190.00 per share in cash, representing a total equity value of approximately $34 billion. The acquisition redefines the cloud market for business. Red Hat’s open hybrid cloud technologies are now paired with the unmatched scale and depth of IBM’s innovation and industry expertise, and sales leadership in more than 175 countries. Together, IBM and Red Hat will accelerate innovation by offering a next-generation hybrid multicloud platform. Based on open source technologies, such as Linux and Kubernetes, the platform will allow businesses to securely deploy, run and manage data and applications on-premises and on private and multiple public clouds."
      ],
      "link": "https://www.redhat.com/en/about/press-releases/ibm-closes-landmark-acquisition-red-hat-34-billion-defines-open-hybrid-cloud-future",
      "document_passages": null
      ]
    }
  ]
}
```


## API

Open the Open API UI: http://localhost:8080/q/swagger-ui/.

<kbd><img src="screenshots/OpenAI-UI.png" /></kbd>

Download the Open API definition from http://localhost:8080/q/openapi or [openapi.yaml](data/openapi.yaml).

The Question Answering service has the same interface as the query endpoint of [Watson Discovery](https://cloud.ibm.com/apidocs/discovery-data#query). Watson Discovery sample for a natural language query:

```
curl -X POST -u "apikey:xxx" --header "Content-Type: application/json" --data "{\"collection_ids\": [\"xxx\"], \"natural_language_query\": \"text:When and for how much did IBM acquire Red Hat?\", \"passages\": {\"enabled\": true, \"fields\": [\"title\", \"text\"]}}" "https://api.us-east.discovery.watson.cloud.ibm.com/instances/xxx/v2/projects/xxx/query?version=2020-08-30"
```

[Output](data/discovery/output.json):

The Watson Discovery API has been 'extended' in two ways.

**1. Answer is first Result**

Rather than returning 'untrained' the document retrieval strategy is 'llm'. The title and the document_id of the first result is 'Answer'. matching_results is the amount of found documents in addition to the first result which is the answer. So there is one more result than found documents.

```
{
  "matching_results": 2,
  "retrievalDetails": {
    "document_retrieval_strategy": "llm"
  },
  "results": [
    {
      "document_id": "Answer",
      "title": "Answer",
      "text": [
        "IBM has acquired Red Hat for $34 billion in October 2018."
      ],
      "link": null,
      "document_passages": null
    }
```

**2. Document Summaries**

To return summaries for each document, document_passages are used. To mark these passages as something special (summaries), 'field' is set to 'summary'. There is one passage (= summary) per result, except of the first result with the answer.

```
"document_passages": [
  {
    "passage_text": "IBM and Red Hat are defining the future of hybrid cloud computing.",
    "field": "text",
    "answers": [
        {
            "answer_text": "IBM and Red Hat are defining the future of hybrid cloud computing.",
            "field": "summary"
        }
    ]
  }
]
```


## Watson Assistant

Sample how the API of this service can be integrated in Watson Assistant:

<kbd><img src="screenshots/Assistant.png" /></kbd>


## Sample Prompt

Answer the question based on the context below.

Context: It's official - IBM has acquired Red Hat! The deal was announced in October 2018. IBM Closes Landmark Acquisition of Red Hat.

IBM (NYSE:IBM) and Red Hat announced today that they have closed the transaction under which IBM acquired all of the issued and outstanding common shares of Red Hat for $190.00 per share in cash, representing a total equity value of approximately $34 billion. The acquisition redefines the cloud market for business. Red Hat’s open hybrid cloud technologies are now paired with the unmatched scale and depth of IBM’s innovation and industry expertise, and sales leadership in more than 175 countries. Together, IBM and Red Hat will accelerate innovation by offering a next-generation hybrid multicloud platform. Based on open source technologies, such as Linux and Kubernetes, the platform will allow businesses to securely deploy, run and manage data and applications on-premises and on private and multiple public clouds.

Question: When and for how much did IBM acquire Red Hat? Summarize the answer.

Answer: 

-> October 2018 for $190.00 per share in cash, representing a total equity value of approximately $34 billion


## PrimeQA

Setup of PrimeQA is documented in the blog [Using PrimeQA For NLP Question Answering](https://www.deleeuw.me.uk/posts/Using-PrimeQA-For-NLP-Question-Answering/).

Open the [dashboard at :82/qa](http://xxx:82/qa) or [OpenAPI at :50052/docs](http://xxx:50052/docs).

```
curl -v -X POST -u "apikey:0123456789" --header "Content-Type: application/json" --data "{   \"query\": \"text:When and for how much did IBM acquire Red Hat?\" }" "http://localhost:8080/query-primeqa" | jq '.'
```


## Push Image to IBM Container Registry

```
docker build -f src/main/docker/Dockerfile.jvm -t question-answering .
ibmcloud login --sso
ibmcloud target -g xxx
export REGISTRY=icr.io
export NAMESPACE=xxx
ibmcloud cr login
docker tag question-answering:latest ${REGISTRY}/${NAMESPACE}/question-answering:latest
docker push ${REGISTRY}/${NAMESPACE}/question-answering:latest
```


## Resources

* [Generative AI for Question Answering Scenarios](https://heidloff.net/article/question-answering-transformers/)
* [Generative AI Sample Code for Question Answering](https://heidloff.net/article/sample-question-answering/)
* [Introduction to Neural Information Retrieval](https://heidloff.net/article/introduction-neural-information-retrieval/)
* [Optimizing Generative AI for Question Answering](https://heidloff.net/article/optimizing-generative-ai-for-question-answering/)
* [Integrating generative AI in Watson Assistant](https://heidloff.net/article/integrating-generative-ai-in-watson-assistant/)
* [Setup of Bring Your Own Search in Watson Assistant](https://github.com/nheidloff/question-answering/tree/main/assistant)
* [Using PrimeQA For NLP Question Answering](https://www.deleeuw.me.uk/posts/Using-PrimeQA-For-NLP-Question-Answering/)
* [Finding concise answers to questions in enterprise documents](https://medium.com/ibm-data-ai/finding-concise-answers-to-questions-in-enterprise-documents-53a865898dbd)
* [Bring your own search to IBM Watson Assistant](https://medium.com/ibm-watson/bring-your-own-search-to-ibm-watson-assistant-587e77410c98)