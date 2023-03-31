# Automation with scripts

## 1. Automated deployment to `Code Engine`

The bash script `deploy_qa_container_to_ce.sh` automates the deployment to Code Engine.

### 1. Create a `.env` file

```sh
cd scripts
cat ./env_template > .env
```

### 2. Edit the `.env` file

```sh
# ******************************
# Question Answering Application 
# configuration for local and on CE

export QA_API_KEY="YOUR_QA_API_KEY"
export MAAS_URL="YOUR_MAAS_UR"
export MAAS_API_KEY="YOUR_MAAS_API_KEY"
export PROXY_API_KEY="YOUR_PROXY_API_KEY"

export DISCOVERY_API_KEY="YOUR_DISCOVERY_API_KEY"
export DISCOVERY_URL="YOUR_DISCOVERY_URL"
export DISCOVERY_INSTANCE="YOUR_DISCOVERY_INSTANCE"
export DISCOVERY_PROJECT="YOUR_DISCOVERY_PROJECT"
export DISCOVERY_COLLECTION_ID="YOUR_DISCOVERY_COLLECTION_ID"

export PRIME_QA_URL="YOUR_PRIME_QA_URL"
export RERANKER_URL="YOUR_RERANKER_URL"
export PROXY_URL="YOUR_PROXY_URL"

# Experiments
export EXPERIMENT_LLM_NAME=google/ul2
export EXPERIMENT_LLM_MIN_NEW_TOKENS=5
export EXPERIMENT_LLM_MAX_NEW_TOKENS=20
export EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS=5
export EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS=10
export EXPERIMENT_RERANKER_MODEL="/store/checkpoints/drdecr/DrDecr.dnn"
export EXPERIMENT_RERANKER_ID=ColBERTReranker
export EXPERIMENT_METRICS_SESSION=$(date +%s)
export EXPERIMENT_METRICS_DIRECTORY=$(pwd)/../metrics

# ******************************
# Related to the deployment on 
# Code Engine in IBM Cloud

# IBM Cloud
export IC_RESOURCE_GROUP=YOUR_IBM_CLOUD_RESOURCE_GROUP
export IC_REGION=YOUR_IBM_CLOUD_REGION
export IC_API_KEY=YOUR_IBM_CLOUD_API_KEY

# Code Engine
export CE_PROJECT_NAME=YOUR_CE_PROJECT_NAME
export CE_CR_USERNAME=YOUR_CE_CONTAINER_REGISTRY_USERNAME
export CE_CR_PASSWORD=YOUR_CE_CONTAINER_REGISTRY_PASSWORD
export CE_CR_EMAIL=YOUR_CR_EMAIL

# Container registry
export CR=YOUR_CONTAINER_REGISTRY
export CR_REPOSITORY=YOUR_CONTAINER_REGISTRY_REPOSITORY
export CI_NAME=YOUR_CONTAINER_IMAGE_NAME
export CI_TAG=YOUR_CONTAINER_IMAGE_TAG
```

### 3. Execute the bash automation

```sh
sh ce_deploy_qa_container.sh
```

* Example output:

```sh

*********************
loginIBMCloud
*********************

API endpoint: https://cloud.ibm.com
...

**********************************
 Using following project: MY-Project-Testing
**********************************
...
A project named 'MY-Project-Testing' with id 'XXXXX' and status 'active' already exists in region 'us-east'. Select a different name.

Selecting project 'MY-Project-Testing'...
OK
Selecting project 'XXX-Project-Testing'...
Added context for 'XXX-Project-Testing' to the current kubeconfig file.
OK
Code Engine project namespace: XXXX
No resources found in XXXXX namespace.
Creating image registry access secret 'XXXX'...
OK
Creating application 'mock-api-testing'...
Configuration 'mock-api-testing' is waiting for a Revision to become ready.
Ingress has not yet been reconciled.
Waiting for load balancer to be ready.
Run 'ibmcloud ce application get -n mock-api-testing' to check the application status.
OK

https://mock-api-testing.XXXX.XXXX.codeengine.appdomain.cloud
For troubleshooting information visit: https://cloud.ibm.com/docs/codeengine?topic=codeengine-troubleshoot-apps.
Run 'ibmcloud ce application events -n mock-api-testing' to get the system events of the application instances.
Run 'ibmcloud ce application logs -f -n mock-api-testing' to follow the logs of the application instances.
OK

Name:               mock-api-testing  
ID:                 XXXX
Project Name:       MY-Project-Testing  
Project ID:         XXXX
Age:                21s  
Created:            2023-03-24T15:43:51+01:00  
URL:                https://mock-api-testing.XXXX.XXXXX.codeengine.appdomain.cloud  
Cluster Local URL:  http://mock-api-testing.XXXX..svc.cluster.local  
Console URL:        https://cloud.ibm.com/codeengine/project/XXXX.XXXXX/application/mock-api-testing/configuration  
Status Summary:     Application deployed successfully  

Environment Variables:    
  Type     Name                                     Value  
  Literal  CE_APP                                   mock-api-testing  
  Literal  CE_DOMAIN                                XXXX.codeengine.appdomain.cloud  
  Literal  CE_SUBDOMAIN                             XXXX  
  Literal  DISCOVERY_API_KEY                        XXXX  
  Literal  DISCOVERY_COLLECTION_ID                  XXXX  
  Literal  DISCOVERY_INSTANCE                       XXXX  
  Literal  DISCOVERY_PROJECT                        XXXX 
  Literal  DISCOVERY_URL                            https://XXXX.discovery.watson.cloud.ibm.com/instances/  
  Literal  EXPERIMENT_LLM_MAX_INPUT_DOCUMENTS       5  
  Literal  EXPERIMENT_LLM_MAX_NEW_TOKENS            25  
  Literal  EXPERIMENT_LLM_MIN_NEW_TOKENS            5  
  Literal  EXPERIMENT_LLM_NAME                      goole/ul2  
  Literal  EXPERIMENT_RERANKER_MAX_INPUT_DOCUMENTS  10  
  Literal  MAAS_API_KEY                             XXXX 
  Literal  MAAS_URL                                 https://XXXX/XX/xxx  
  Literal  PRIME_QA_URL                             http://XXXX:50059/ask  
  Literal  PROXY_API_KEY                            XXXX
  Literal  PROXY_URL                                https://XXXX/xxxx
  Literal  QA_API_KEY                               XXXXX
  Literal  RERANKER_URL                             http://XXXXXX:50052/RerankRequest  
Image:                  my_registry/my_orgr/question-answering:13  
Resource Allocation:      
  CPU:                1  
  Ephemeral Storage:  400M  
  Memory:             4G  
Registry Secrets:         
  icr.io  
Port:                   8080  

Revisions:     
  mock-api-testing-00001:    
    Age:                20s  
    Latest:             true  
    Traffic:            100%  
    Image:              my_registry/my_org/question-answering:13 (pinned to 423895)  
    Running Instances:  1  

Runtime:       
  Concurrency:    100  
  Maximum Scale:  10  
  Minimum Scale:  1  
  Timeout:        300  

Conditions:    
  Type                 OK    Age  Reason  
  ConfigurationsReady  true  4s     
  Ready                true  0s     
  RoutesReady          true  0s     

Events:        
  Type    Reason   Age  Source              Messages  
  Normal  Created  22s  service-controller  Created Configuration "mock-api-testing"  
  Normal  Created  22s  service-controller  Created Route "mock-api-testing"  

Instances:     
  Name                                                Revision                Running  Status   Restarts  Age  
  mock-api-testing-00001-deployment-7b8fXXX-vjjcs  mock-api-testing-00001  3/3      Running  0         21s  
************************************
Access the application mock-api-testing - URL: https://mock-api-testing.XXXX.XXXX.codeengine.appdomain.cloud
************************************
************************************
 Kubernetes info 'mock-api-testing': pods, deployments and configmaps details 
************************************
NAME                                                 READY   STATUS    RESTARTS   AGE
mock-api-testing-00001-deployment-7bXXXXf-vjjcs   3/3     Running   0          25s
NAME                                READY   UP-TO-DATE   AVAILABLE   AGE
mock-api-testing-00001-deployment   1/1     1            1           26s
NAME                 DATA   AGE
istio-ca-root-cert   1      4h27m
kube-root-ca.crt     1      4h27m
************************************
 Kubernetes mock-api-testing: log
************************************
************************************
Show log for the pod: mock-api-testing-00001-deployment-7b8f7bd44f-vjjcs
************************************
Starting the Java application using /opt/jboss/container/java/run/run-java.sh ...
INFO exec  java -Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager -Xms477m -Xmx1907m -XX:+UseParallelGC -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90 -XX:+ExitOnOutOfMemoryError -cp "." -jar /deployments/quarkus-run.jar 
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2023-03-24 14:44:07,724 INFO  [io.qua.sma.ope.run.OpenApiRecorder] (main) CORS filtering is disabled and cross-origin resource sharing is allowed without restriction, which is not recommended in production. Please configure the CORS filter through 'quarkus.http.cors.*' properties. For more information, see Quarkus HTTP CORS documentation
2023-03-24 14:44:08,057 INFO  [io.quarkus] (main) question-answering 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.16.4.Final) started in 3.159s. Listening on: http://0.0.0.0:8080
2023-03-24 14:44:08,058 INFO  [io.quarkus] (main) Profile prod activated. 
2023-03-24 14:44:08,058 INFO  [io.quarkus] (main) Installed features: [cdi, rest-client-reactive, rest-client-reactive-jackson, resteasy-reactive, resteasy-reactive-jsonb, smallrye-context-propagation, smallrye-openapi, vertx]
```

## 2. Automated `kubectl` logging for the application pod in `Code Engine`

```sh
sh ce_log_qa_container.sh
```