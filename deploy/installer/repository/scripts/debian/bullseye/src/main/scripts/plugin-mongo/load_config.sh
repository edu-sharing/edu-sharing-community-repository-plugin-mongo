#!/bin/bash
set -e
set -o pipefail

########################################################################################################################

repository_mongo_db_protocol="${REPOSITORY_MONGO_PROTOCOL:-"mongodb"}"
repository_mongo_db_host="${REPOSITORY_MONGO_HOST:-"127.0.0.1"}"
repository_mongo_db_port="${REPOSITORY_MONGO_PORT:-27017}"
repository_mongo_db_user="${REPOSITORY_MONGO_USER:-"repository"}"
repository_mongo_db_pass="${REPOSITORY_MONGO_PASS:-"repository"}"
repository_mongo_db_database="${REPOSITORY_MONGO_DATABASE:-"edu-sharing"}"

export repository_mongo_db_protocol;
export repository_mongo_db_host;
export repository_mongo_db_port;
export repository_mongo_db_user;
export repository_mongo_db_pass;
export repository_mongo_db_database;

########################################################################################################################
