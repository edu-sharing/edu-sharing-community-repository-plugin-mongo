#!/bin/bash
set -e
set -o pipefail

########################################################################################################################

repository_mongo_db_connection_string="${REPOSITORY_MONGO_CONNECTION_STRING:-"mongodb://repository:repository@127.0.0.1:27017/edu-sharing"}"
repository_mongo_db_database="${REPOSITORY_MONGO_DATABASE:-"edu-sharing"}"

export repository_mongo_db_protocol;
export repository_mongo_db_host;
export repository_mongo_db_port;
export repository_mongo_db_user;
export repository_mongo_db_pass;
export repository_mongo_db_database;

########################################################################################################################
