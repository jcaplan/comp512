#!/bin/bash

HOST=localhost
CLIENTMAIN=Client


while [[ $# > 1 ]]
do
	key="$1"

	case $key in
	    -h)
	    HOST="$2"
	    shift # past argument
	    ;;
	    -m)
	    CLIENTMAIN="$2"
	    shift # past argument
	    ;;
	    *)
	            # unknown option
	    ;;
	esac
	shift # past argument or value
done


echo "host = ${HOST}"
echo "main = ${CLIENTMAIN}"



echo "using host: ${HOST}"

ant client -Dclient-service.name=rmMW -Dclient-service.port=18080 -Dclient-service.host=${HOST} \
			-Dclient-main=${CLIENTMAIN}