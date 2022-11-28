#!/bin/bash
set -eu

########################################################################################################################

debug_repository_plugin_mongo="${DEBUG_REPOSITORY_PLUGIN_MONGO:-}"

#repository_mongo_protocol="${REPOSITORY_MONGO_PROTOCOL:-mongodb}"
#repository_mongo_host="${REPOSITORY_MONGO_HOST:-repository-mongo}"
#repository_mongo_port="${REPOSITORY_MONGO_PORT:-27017}"
#repository_mongo_user="${REPOSITORY_MONGO_USER:-repository}"
#repository_mongo_pass="${REPOSITORY_MONGO_PASS:-repository}"
repository_mongo_connection_string="${REPOSITORY_MONGO_CONNECTION_STRING}"
repository_mongo_database="${REPOSITORY_MONGO_DATABASE:-edu-sharing}"

eduSConf="tomcat/shared/classes/config/cluster/edu-sharing.deployment.conf"

if [[ -z $repository_mongo_connection_string ]] ; then
  echo "connectionString not setup!"
  exit 1;
fi

### Alfresco platform ##################################################################################################

### edu-sharing platform ###############################################################################################

hocon -f "${eduSConf}" \
	set "mongo.connectionString" '"'"${repository_mongo_connection_string}"'"'

hocon -f "${eduSConf}" \
	set "mongo.database" '"'"${repository_mongo_database}"'"'
