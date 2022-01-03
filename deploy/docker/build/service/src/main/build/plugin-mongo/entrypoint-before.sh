#!/bin/bash
set -eux

echo "plugin-mongo: entrypoint-before"

repository_mongo_host="${REPOSITORY_MONGO_HOST:-repository-mongo}"
repository_mongo_port="${REPOSITORY_MONGO_PORT:-27017}"
repository_mongo_user="${REPOSITORY_MONGO_USER:-repository}"
repository_mongo_pass="${REPOSITORY_MONGO_PASS:-repository}"

until wait-for-it "${repository_mongo_host}:${repository_mongo_port}" -t 3; do sleep 1; done