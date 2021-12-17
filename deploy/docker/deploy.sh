#!/bin/bash
set -e
set -o pipefail

case "$(uname)" in
MINGW*)
	COMPOSE_EXEC="winpty docker-compose"
	;;
*)
	COMPOSE_EXEC="docker-compose"
	;;
esac
export COMPOSE_EXEC

reload() {

	echo "Reloading ..."

	ROOT_PATH="$(
  	cd "$(dirname ".")"
  	pwd -P
  )"

  case $1 in
  	/*) pushd "$1" >/dev/null || exit ;;
  	*) pushd "$(dirname "$ROOT_PATH/$1")" >/dev/null || exit ;;
  esac

	COMPOSE_FILE="$(pwd)/$(basename "$1")"
	export COMPOSE_FILE
	popd >/dev/null || exit

	$COMPOSE_EXEC \
		-f "$COMPOSE_FILE" \
		-p edu-sharing-plugin-mongo \
		exec repository-service bash \
		-c "java -jar bin/alfresco-mmt.jar uninstall amps/edu-sharing/1 -directory -nobackup -force; java -jar bin/alfresco-mmt.jar install amps/edu-sharing/1 tomcat/webapps/edu-sharing -directory -nobackup -force; touch tomcat/webapps/edu-sharing/WEB-INF/web.xml;"
}

usage() {
	echo ""
	echo "Usage: ${CLI_CMD} [option]"
	echo ""
	echo "Option:"
	echo ""
}

while true; do
	flag="$1"
	shift || break

	case "$flag" in
	--reload) reload "$1" && exit 0 ;;
	*)
		{
			echo "error: unknown flag: $flag"
			usage
		} >&2
		exit 1
		;;
	esac
done