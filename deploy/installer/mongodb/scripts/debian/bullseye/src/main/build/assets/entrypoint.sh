#!/bin/bash

# this script is docker specific


initializeMongoDb() {
  if [[ -f mongodb_init ]] ; then
    return
  fi

  mongo 'admin' <<< "db.getSiblingDB('admin').createUser({ user: '$MONGODB_ROOT_USER', pwd: '$MONGODB_ROOT_PASSWORD', roles: [{role: 'root', db: 'admin'}] })"
  mongo 'admin' <<< "db.getSiblingDB('$MONGODB_DATABASE').createUser({ user: '$MONGODB_USERNAME', pwd: '$MONGODB_PASSWORD', roles: [{role: 'readWrite', db: '$MONGODB_DATABASE'}] })"

  #mongo 'admin' dbInit.js
  touch mongodb_init
}

systemctl start mongod

initializeMongoDb

exec "$@"