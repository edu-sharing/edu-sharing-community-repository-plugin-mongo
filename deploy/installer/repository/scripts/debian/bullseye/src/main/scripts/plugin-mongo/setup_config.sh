#!/bin/bash
set -e
set -o pipefail

eduSConf="tomcat/shared/classes/config/cluster/edu-sharing.deployment.conf"

########################################################################################################################

echo "- update edu-sharing env for plugin elastic"

pushd "$ALF_HOME" &> /dev/null

### edu-sharing ########################################################################################################


hocon -f "${eduSConf}" \
	set "mongo.connectionString" '"'"${repository_mongo_protocol}://${repository_mongo_user}:${repository_mongo_pass}@${repository_mongo_host}:${repository_mongo_port}"'"'

hocon -f "${eduSConf}" \
	set "mongo.database" "${repository_mongo_database}"


########################################################################################################################
