# Automation with scripts

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