#!/bin/bash

# this script is docker specific

systemctl start mongod

if [[ ! -f mongodb_init ]] ; then
	mongo 'admin' dbInit.js
  touch mongodb_init
fi

exec "$@"