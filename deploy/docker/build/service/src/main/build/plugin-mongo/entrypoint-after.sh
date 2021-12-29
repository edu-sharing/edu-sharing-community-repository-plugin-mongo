#!/bin/bash
set -eux

echo "plugin-mongo: entrypoint-after"

repository_mongo_host="${REPOSITORY_MONGO_HOST:-repository-mongo}"
repository_mongo_port="${REPOSITORY_MONGO_PORT:-27017}"
repository_mongo_user="${REPOSITORY_MONGO_USER:-repository}"
repository_mongo_pass="${REPOSITORY_MONGO_PASS:-repository}"

hocon -f tomcat/shared/classes/config/edu-sharing.deployment.conf \
	set "mongo.servers" '["'"${repository_mongo_host}:${repository_mongo_port}"'"]'

hocon -f tomcat/shared/classes/config/edu-sharing.deployment.conf \
	set "mongo.username" "${repository_mongo_user}"

hocon -f tomcat/shared/classes/config/edu-sharing.deployment.conf \
	set "mongo.password" "${repository_mongo_pass}"
