#!/bin/bash
set -eux

repository_mongo_host="${REPOSITORY_MONGO_HOST:-repository-mongo}"
repository_mongo_port="${REPOSITORY_MONGO_PORT:-27017}"
repository_mongo_user="${REPOSITORY_MONGO_USER:-repository}"
repository_mongo_pass="${REPOSITORY_MONGO_PASS:-repository}"

eduSConf="tomcat/shared/classes/config/cluster/edu-sharing.deployment.conf"

### Wait ###############################################################################################################

until wait-for-it "${repository_mongo_host}:${repository_mongo_port}" -t 3; do sleep 1; done

### Alfresco platform ##################################################################################################

### edu-sharing platform ###############################################################################################

hocon -f "${eduSConf}" \
	set "mongo.servers" '["'"${repository_mongo_host}:${repository_mongo_port}"'"]'

hocon -f "${eduSConf}" \
	set "mongo.username" "${repository_mongo_user}"

hocon -f "${eduSConf}" \
	set "mongo.password" "${repository_mongo_pass}"
