# Automation CE deployment

The automation does:

1. Verify Docker is running
2. Log in to IBM Cloud
3. Build and push the container image to IBM Cloud Container Image Registry (_the container tag is the last GitHub commit id_)
4. Create a new Code Engine project
5. Create an IBM Cloud Container Image Registry access for the Code Engine project
6. Deploys the qa service to Code Engine
7. Shows the plain `kubectl` information for the containers in the project
8. Shows the plain `kubectl` log information for the first container
9. Set global environment variable for later usage
10. Saves the deployment configurations in the `deployment-log` folders `all` and `last`

## 1. Clone the project

```sh
git clone https://github.com/nheidloff/question-answering.git
cd question-answering
```

## 2. Create the needed `.env` files

* Set the Code Engine and IBM Cloud environment

```sh
cat ./scripts/.env_template >  ./scripts/.env
```

* Set the Question answering service environment

```sh
cat ./service/.env_template >  ./service/.env
```

## 3. Run the automation

```sh
cd scripts/ce-deployment
sh deploy-to-code-engine.sh
```
