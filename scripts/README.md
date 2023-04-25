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

| Combination | Scripts | Experiment-runner<br />configuation | Config | Notes |
| --- | --- | --- | --- | --- |
| 1 | [start_apps.sh](./start_apps.sh) | Example [`.env_template file`](../metrics/experiment-runner/example_templates/.env_combination_1_example) | LocalApp : LocalApp | Both applications using the `output` and `input` folders on the local machine. |
| 2 | [start_containers.sh](./start_containers.sh)<br />[stop-containers.sh](./stop_containers.sh) | Example [`.env_template file`](../metrics/experiment-runner/example_templates/.env_combination_1_example) | LocalContainer:LocalContainer | Both applications using the `output` and `input` folders on the local machine. <br />The `experiment-runner` must be started in a new terminal session with a `docker exec` command. |
| 3 | Container in `Code Engine` | Local container in Docker | CloudContainer:LocalContainer | Only the `experiment-runner` application uses the `output` and `input` folders on the local machine. <br />The experiment must be started in a *new terminal* session with a `docker exec` command. |
| 4 | Container in `on Code Engine` | Local Python application | CloudContainer:LocalApp | Only the `experiment-runner` application uses the `output` and `input` folders on the local machine. |







## 1. Automated start of QA - pipeline service and experiment-runner as applications

The experiment-runner is a Python command line application and the QA pipeline service is a Quarkus server application.

The bash script `start_apps.sh` automates the start of both applications

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

The automation starts the bash automation for [experiment-runner](./exp_runner_local.sh) and for the [QA pipeline service](./qa_local.sh).

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