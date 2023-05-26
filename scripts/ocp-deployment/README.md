# Open Shift deployment

The automated deployment fully deploys the [`question answering service`](./../../service/) to an IBM Cloud Red Hat OpenShift cluster in a Virtual Private Cloud environment. The automation uses bash scripting and a [`Helm Chart`](./charts/question-answering-helm/).

The automation does this deployment for you:

1. It builds and pushes a new container image to the `IBM Cloud Container registry`. The image will be in the following format: `"xxx.icr.io/your-name-space/question-answering:last-git-commit-id"`

2. It creates OpenShift deployment using a Helm Chart for the created container image `question answering service`. 

3. The `question answering service` configuration is based on the given environment specification in the `/service/.env` file.

4. It invokes the `question answering service` REST query endpoint with an example question.

5. It saves all configurations it has used in a log folder.

6. You can restore a deployment from existing information:

* Repository URL
* Commit ID
* ".env" file with the working configuration 

```sh
export RESTORE_COMMAND=restore
export COMMIT_ID=XXXXXXX
export REPOSITORY_URL=https://github.com/nheidloff/question-answering
export ENVIORNMENT_FILENAME=my-restore.env
sh deploy-to-openshift.sh $RESTORE_COMMAND $COMMIT_ID $REPOSITORY_URL $ENVIORNMENT_FILENAME
```

## Usage

This instruction is a short guide on how to use the automation.

### Configuration of the environment

Ensure you configured the following environment variables to your needs.

* [/service/.env](./../../service/.env_template) that file contains the specification of the `question answering service`.

* [/scripts/.env/](./../../scripts/.env_template) that file contains the configuration for the deployment on IBM Cloud.

* [/scripts/ocp-deployment/](./../../scripts/ocp-deployment.env_template) that file contains some specific configuration of the OCP deployment.

###  Execution of the automation
1. Navigate to the ocp-deployment directory

```sh
cd /scripts/ocp-deployment
```

2. Start the script execution

```sh
sh deploy-to-openshift.sh
```

* Interactive output:

    1. The script opens a browser window where you can log on to IBM Cloud and get a passcode to log in. Next, insert the passcode from the webpage.

    ```sh
    Insert passcode: 
    ```

    2. Verify if the Helm deployment configuration fits your needs.

    ```sh
    Verify the try run. Move on Y/N?
    Y
    ```

3. Inspect the created logs for your deployment configuration in `scripts/ocp-deployment/logs` .

```sh
logs
├── 2023-05-16-13:53:25-git-56fe86c0474720f9fb39f9619baa8e735de5b022
│   ├── code.txt
│   ├── deployment-info.txt
│   ├── helm-generate-values-file.sh
│   ├── helm-values.yaml
│   ├── ibm-cloud.env
│   ├── ocp-deployment.env
│   └── service.env
└── README.md
```