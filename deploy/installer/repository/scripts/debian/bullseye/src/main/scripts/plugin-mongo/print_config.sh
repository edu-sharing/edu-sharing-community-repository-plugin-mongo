#!/bin/bash
set -e
set -o pipefail

########################################################################################################################

echo "#########################################################################"
echo ""
echo "plugin: mongo"
echo ""
echo "    Protocol:          ${repository_mongo_db_protocol}"
echo "    Host:              ${repository_mongo_db_host}"
echo "    Port:              ${repository_mongo_db_port}"
echo "    User:              ${repository_mongo_db_user}"
echo "    Password:          ${repository_mongo_db_pass}"
echo "    Database:          ${repository_mongo_db_database}"
echo ""

########################################################################################################################
