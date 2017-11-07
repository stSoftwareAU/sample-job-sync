#!/bin/bash

CONFIG_FILE_PATH="$1"

if [ ! -z "${CONFIG_FILE_PATH}" ]
then
    java -jar ../ms-payment-sync-jar-with-dependencies.jar "-c${CONFIG_FILE_PATH}"
else
    echo "configuration file is required"
fi




