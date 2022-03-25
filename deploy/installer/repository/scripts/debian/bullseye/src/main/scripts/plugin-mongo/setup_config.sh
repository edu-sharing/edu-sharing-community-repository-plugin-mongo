#!/bin/bash
set -e
set -o pipefail

eduSConf="tomcat/shared/classes/config/cluster/edu-sharing.deployment.conf"

########################################################################################################################

echo "- update edu-sharing env for plugin elastic"

pushd "$ALF_HOME" &> /dev/null

### edu-sharing ########################################################################################################


hocon -f "${eduSConf}" \
	set "mongo.connectionString" '"'"${repository_mongo_db_connection_string}"'"'

hocon -f "${eduSConf}" \
	set "mongo.database" '"'"${repository_mongo_database}"'"'


########################################################################################################################
