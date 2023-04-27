#!/bin/bash

CURRENT_USER=$(whoami)
#echo "Current user: $CURRENT_USER"

#echo "*********************"
#echo "** Create enviroment file "
#echo "*********************"

"/bin/sh" ./generate_env-config.sh > ./.env
#cat .env

echo "*********************"
echo " To start the experiment-runner"
echo " open a new terminal session"
echo " and execute following command:"
echo " 'docker exec -it experimentrunner sh ./start.sh'"
echo "*********************"

echo "*********************"
echo "** Start dummy Python server"
echo "*********************"

python server.py