#!/bin/bash

repository_mongo_host="${REPOSITORY_MONGO_HOST:-"127.0.0.1"}"
repository_mongo_port="${REPOSITORY_MONGO_PORT:-27017}"

until wait-for-it "${repository_mongo_host}:${repository_mongo_port}" -t 3; do sleep 1; done