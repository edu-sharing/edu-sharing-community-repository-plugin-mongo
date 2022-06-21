#!/bin/bash

repository_mongo_db_connection_string="${REPOSITORY_MONGO_CONNECTION_STRING}"

if [[ -z $repository_mongo_db_connection_string ]] ; then
  echo "connectionString not setup!"
  exit 1;
fi