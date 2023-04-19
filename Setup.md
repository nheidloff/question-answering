# Setup

There are several ways to run the code:

1. All three containers via Docker Compose
2. Only the Question Answering service as container
3. As local Java and Python applications

Two or three online components need to be set up and/or configured:

1. Watson Discovery
2. PrimeQA ReRanker
3. Model as a Service provider


## Getting Started

The easiest way to get started is to use a sample dataset and to run as much as possible locally.

1. Clone the repo
2. Provision Watson Discovery, upload data and point to it from your local environment
3. Set up the PrimeQA ReRanker in a VM and point to it from your local environment
4. Start three local containers
5. Configure your experiment and start it


### Getting Started - Step 1: Clone Repo

```
git clone https://github.com/nheidloff/question-answering.git
```


### Getting Started - Step 2: Watson Discovery

Set up [Watson Discovery](https://www.ibm.com/cloud/watson-discovery) in the IBM Cloud for free.

Create a new project and upload the [demo dataset](data/demo/passages.json) in a new collection. The dataset contains four documents which have been split in smaller passages.

Download the following information which you'll need later.

1. DISCOVERY_API_KEY=xxx
2. DISCOVERY_URL=xxx
3. DISCOVERY_COLLECTION_ID=xxx
4. DISCOVERY_PROJECT=xxx
5. DISCOVERY_INSTANCE=xxx


### Getting Started - Step 3: PrimeQA

Follow the instruction in [Using PrimeQA For NLP Question Answering](https://www.deleeuw.me.uk/posts/Using-PrimeQA-For-NLP-Question-Answering/) how to set up PrimeQA in a VM.

Additionally you need to download the DrDecr model and run the ReRanker container.

```
ssh root@<my-host-ip>
git clone https://github.com/primeqa/primeqa.git
cd primeqa/
docker build -f Dockerfiles/Dockerfile.cpu -t primeqa:$(cat VERSION) --build-arg image_version:$(cat VERSION) .
cd ~/<my-dir>/create-primeqa-app/primeqa-store/checkpoints/
mkdir drdecr
cd drdecr
wget https://huggingface.co/PrimeQA/DrDecr_XOR-TyDi_whitebox/resolve/main/DrDecr.dnn
cd ~/<my-dir>/create-primeqa-app
docker run --detach --name primeqa-rest -it -p 50052:50052 --mount type=bind,source="$(pwd)"/primeqa-store,target=/store --mount type=bind,source="$(pwd)"/cache/huggingface/,target=/cache/huggingface/ -e STORE_DIR=/store -e mode=rest -e require_ssl=false primeqa:0:14:1
```

Check whether you can open 'my-host-ip:50052/rerankers'.


### Getting Started - Step 4: Start local Containers

There are three containers:

1. Question Answering
2. Experiment Runner
3. MaaS mock

*Question Answering: .env*

Before the question answering service and the experiment runner can be run, configuration needs to be done. Define the six mandatory variables in [service/.env](service/.env_template). 

```
cd service
cp .env_template .env
code .env
```

*Experiment Runner: .env*

You also need to create an [metrics/experiment-runner/.env](metrics/experiment-runner/.env_template) file. To run the demo, no further configuration needs to be done, but the file needs to be created since it is not pushed to Git.

```
cd metrics/experiment-runner
cp .env_template .env
```

To start the containers Docker Compose is used so that networking between them is easy:

```
cd scripts
sh start-containers.sh
```


### Getting Started - Step 5: Start Experiment

The experiment is started by executing a script in the experiment runner container. It invokes the two queries defined in the [ground truth](metrics/input/demo-ground-truth.xlsx) document.

```
docker exec -it experimentrunner sh
sh start.sh
```

After invoking REST APIs the results are stored in the 'metrics/output' directory:

* [xxx-Last-Run.md](metrics/output/demo-Last-Run.md)
* [xxx_output.xlsx](metrics/output/demo_output.xlsx)
* [xxx-Runs.csv](metrics/output/demo-Runs.csv)
* [xxx-Metadata.csv](metrics//output/demo-Metadata.csv)


## Usage of a real MaaS Service

The previous steps explained how to run a MaaS mock locally for a quick demo of the flow. For real question answering services and experiments real large language models are used to generate answers. Since these models are typically big, it makes sense to use a Model as a Service offering. 

Define the MaaS access information in [service/.env](service/.env_template). If your MaaS provider uses another interface, you need extend the MaaS mock to become a proxy.

Additionally you need to upload your own data to Watson Discovery. Your ground truth document needs to be define in [metrics/experiment-runner/.env](metrics/experiment-runner/.env_template).

There are also several optional variables which can be changed in [service/.env](service/.env_template), for example:

* EXPERIMENT_LLM_NAME=google/flan-t5-xxl
* EXPERIMENT_LLM_PROMPT="Document: CONTEXT\n\nQuestion: QUESTION\nAnswer the question using the above document. Answer: "
* EXPERIMENT_LLM_MIN_NEW_TOKENS=1
* EXPERIMENT_LLM_MAX_NEW_TOKENS=300
* EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS=3
* EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS=30
* EXPERIMENT_DISCOVERY_CHARACTERS=1000
* EXPERIMENT_DISCOVERY_FIND_ANSWERS=false
* EXPERIMENT_RERANKER_MODEL="/store/checkpoints/drdecr/DrDecr.dnn"
* EXPERIMENT_RERANKER_ID=ColBERTReranker
* EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS=20
* MAX_RESULTS=5

After the variables have been changed, the containers need to be restarted.


## Run Question Answering and Experiment Runner locally

During development it is faster to run the applications without containers.

*Question Answering*

Via Java 17 and Maven 3.9:

```
cd servive
mvn packages
source .env
echo $EXPERIMENT_METRICS_SESSION
echo $QA_API_KEY
mvn quarkus:dev
```

*Experiment Runner*

Define $QA_API_KEY and $EXPERIMENT_METRICS_SESSION from previous step. Define the name and the location of your ground truth file 'input_excel_filename'.

```
cd metrics/experiment-runner
code .env
```

Via Python 3.9:

```
cd metrics/experiment-runner
python3 -m pip install requests pandas datasets huggingface_hub fsspec aiohttp csv sacrebleu python-dotenv pyinstaller evaluate openpyxl absl nltk rouge_score
source .env
python3 exp-runner.py
```


## Run Question Answering as Container locally

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
  -e EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS=${EXPERIMENT_DISCOVERY_MAX_OUTPUT_DOCUMENTS} \
  -e EXPERIMENT_DISCOVERY_CHARACTERS=${EXPERIMENT_DISCOVERY_CHARACTERS} \
  -e EXPERIMENT_DISCOVERY_FIND_ANSWERS=${EXPERIMENT_DISCOVERY_FIND_ANSWERS} \
  -e EXPERIMENT_LLM_PROMPT=${EXPERIMENT_LLM_PROMPT} \
  -v $(pwd)/../metrics/output:/deployments/metrics \
  question-answering:latest
```


## Code Engine Deployment

Push Question Answering Image to IBM Container Registry:

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

Invoke API:

```
QA_API_KEY=xxx
QUERY="xxx"
curl -X POST \
    -u "apikey:$QA_API_KEY" \
    --header "Content-Type: application/json" \
    --data "{\"query\": \"text:$QUERY\"}" \
    "https://xxx.xxx.us-east.codeengine.appdomain.cloud/query" \
    | jq '.'
```