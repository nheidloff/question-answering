# Automation with scripts

There are various options to start an experiment.
An experiment needs to run a minimum following applications.

1. QA - Service
2. Experiment-runner

_Note:_ The following combinations are possible when `Model as a Service` is available on the Cloud.

| Combination | QA - Service - runtime | Experiment-runner - runtime | Config | Notes |
| --- | --- | --- | --- | --- |
| 1 | Local Quarkus application | Local Python application | LocalApp : LocalApp | Both applications using the `output` and `input` folders on the local machine. |
| 2 | Container in local `Docker compose` | Container in local `Docker compose` | LocalContainer:LocalContainer | Both applications using the `output` and `input` folders on the local machine. <br />The `experiment-runner` must be started in a new terminal session with a `docker exec` command. |
| 3 | Container in `Code Engine` | Local container in Docker | CloudContainer:LocalContainer | Only the `experiment-runner` application uses the `output` and `input` folders on the local machine. <br />The experiment must be started in a *new terminal* session with a `docker exec` command. |
| 4 | Container in `on Code Engine` | Local Python application | CloudContainer:LocalApp | Only the `experiment-runner` application uses the `output` and `input` folders on the local machine. |

* Automation scripts and environment configuration

| Combination | Scripts | Experiment-runner<br />configuation | QA-service<br />configuration  | Notes |
| --- | --- | --- | --- | --- |
| 1 | [start-apps.sh](./start-apps.sh) | Example [`.env_template file`](../metrics/experiment-runner/example_templates/.env_combination_1_example) |  Example [`.env_template file`](../service/.env_template) |  |
| 2 | [start-containers.sh](./start-containers.sh)<br />[stop-containers.sh](./stop-containers.sh) | Example [`.env_template file`](../metrics/experiment-runner/example_templates/.env_combination_2_example) | Example [`.env_template file`](../service/.env_template) |  |
| 3 | [deploy-to-code-engine.sh](./deploy-to-code-engine.sh)<br />[codeengine_experiment.sh](./start-experiment-code-engine.sh) | Example [`.env_template file`](../metrics/experiment-runner/example_templates/.env_combination_3_example) | Example [`.env_template file`](../service/.env_template) |  |
| 4 |  [codeengine_deploy.sh](./codeengine_deploy.sh) | Example [`.env_template file`](../metrics/experiment-runner/example_templates/.env_combination_4_example)  | Example [`.env_template file`](../service/.env_template) |  |

# Running different combinations of the automated start of `question-answering service` and `experiment-runner`

## 1.  Start applications

The experiment-runner is a Python command line application and the `question-answering service` is a Quarkus server application.

The bash script `start-apps.sh` automates the start of both applications locally.

### 1.1 Create an experiment-runner `.env` file

Ensure you have created the needed environment variables file and adjusted it to your needs. 

```sh
cd ./metrics/experiment-runner
cat ./env_template > .env
```

### 1.2 Create a QA pipeline service `.env` file

Ensure you have created the needed environment variables file and adjusted it to your needs. 

```sh
cd ./service/experiment-runner
cat ./env_template > .env
```

### 1.3 Start the automation

The bash automation `start_apps.sh` starts the automation for [experiment-runner](./local_exp_runner.sh) and the [QA pipeline service](./local_qa_service.sh).

It also creates an `~/.env_profile` file to save global variables.

```sh
sh start_apps.sh
```

* Example output:

```sh
************************************
Environment configuration
************************************
- HOME_PATH :          /YOUR_PATH/scripts
- SESSION_ID:          1681741227
************************************
Environment configuration save in '~/.env_profile'
************************************
************************************
- Enable bash automation for execution
************************************
************************************
- Open terminals
************************************
- QA Service
- Experiment runner
```

## 2.  Start containers

The start of three containers.

* Experiment-runner
* Question-answering servie
* MaaS mock

### 2.1 Create an experiment-runner `.env` file

Ensure you have created the needed environment variables file and adjusted it to your needs. 

```sh
cd ./metrics/experiment-runner
cat ./env_template > .env
```

### 2.2 Create a QA pipeline service `.env` file

Ensure you have created the needed environment variables file and adjusted it to your needs. 

```sh
cd ./service/experiment-runner
cat ./env_template > .env
```

### 3.3 Start the automation

The bash automation `start_containers.sh` the execution of Docker.

It also creates an `~/.env_profile` file to save global variables.

```sh
sh start_containers.sh
```

* Example output:

```sh
************************************
 Build and start containers with Docker compose 
- 'QA-Service'
- 'Experiment-runner'
- 'Maas-mock'
************************************
Home path:    /YOUR_PATH/question-answering/scripts
Session ID:   1684308505
/bin/sh: /YOUR_PATH/git-question-answering/scripts/env_profile_generate.sh: No such file or directory
/YOUR_PATH/git-question-answering/metrics/input
Docker Compose version v2.17.2
**************** BUILD ******************
....
**************** START ******************
...
Attaching to experimentrunner, maasmock, qaservice
...
```

### 3.3 Start experiment



```sh
CONTAINER=$(docker ps | grep experimentrunner | awk '{print $7;}')
docker exec -it experimentrunner sh
docker compose -f ./docker_compose.yaml stop
```