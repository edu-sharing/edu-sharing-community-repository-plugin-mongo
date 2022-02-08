#!/bin/bash
set -eux


debug_repository_plugin_mongo="${DEBUG_REPOSITORY_PLUGIN_MONGO:-}"
repository_mongo_protocol="${REPOSITORY_MONGO_PROTOCOL:-mongodb}"
repository_mongo_host="${REPOSITORY_MONGO_HOST:-repository-mongo}"
repository_mongo_port="${REPOSITORY_MONGO_PORT:-27017}"
repository_mongo_user="${REPOSITORY_MONGO_USER:-repository}"
repository_mongo_pass="${REPOSITORY_MONGO_PASS:-repository}"
repository_mongo_database="${REPOSITORY_MONGO_DATABASE:-edu-sharing}"

eduSConf="tomcat/shared/classes/config/cluster/edu-sharing.deployment.conf"


#### Enable Debug Logs ##################################################################################################
#if [[ "${debug_repository_plugin_mongo}" = 'true' ]] ; then
#  echo "Enable log level debug for repository-plugin-mongo"
#
#  xmlstarlet ed -L \
#  --subnode '/Configuration/Loggers' --type elem -n Logger -v "" \
#  --var node '$prev' \
#  -i '$node' -t attr -n 'name' -v 'org.edu-sharing.plugin_mongo' \
#  -i '$node' -t attr -n 'level' -v 'debug' \
#  'tomcat/webapps/edu-sharing/WEB-INF/log4j2.xml'
#fi

### Wait ###############################################################################################################

until wait-for-it "${repository_mongo_host}:${repository_mongo_port}" -t 3; do sleep 1; done

### Alfresco platform ##################################################################################################

### edu-sharing platform ###############################################################################################

hocon -f "${eduSConf}" \
	set "mongo.connectionString" '"'"${repository_mongo_protocol}://${repository_mongo_user}:${repository_mongo_pass}@${repository_mongo_host}:${repository_mongo_port}"'"'

hocon -f "${eduSConf}" \
	set "mongo.database" "${repository_mongo_database}"
