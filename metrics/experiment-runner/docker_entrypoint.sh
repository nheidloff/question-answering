#!/bin/bash

CURRENT_USER=$(whoami)
echo "Current user: $CURRENT_USER"

echo "*********************"
echo "** Create enviroment file "
echo "*********************"

"/bin/sh" ./generate_env-config.sh > ./.env
cat .env

echo "*********************"
echo "** Start python server"
echo "*********************"

python server.py