#!/bin/bash
set -eux

echo "pluign-mongo: entrypoint.sh"
repository_mongo_host="${REPOSITORY_MONGO_HOST:-repository-search-elastic}"
repository_mongo_port="${REPOSITORY_MONGO_PORT:-9200}"
repository_mongo_user="${REPOSITORY_MONGO_USER:-repository}"
repository_mongo_pass="${REPOSITORY_MONGO_PASS:-repository}"



#hocon -f tomcat/shared/classes/config/edu-sharing.deployment.conf \
#	set "elasticsearch.servers" '["'"${repository_mongo_host}:${repository_mongo_port}"'"]'
