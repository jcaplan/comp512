#!/bin/bash



if [ -z "$1" ]
  then
	HOST="localhost"    
else
	HOST=$1
fi

echo "using host: ${HOST}"


ant middleware -Dserver-service.name=rmMW -Dserver-service.port=18080 \
		-Dmw-service1.name=rmCar -Dmw-service1.port=18081 -Dmw-service1.host=${HOST} \
		-Dmw-service2.name=rmRoom -Dmw-service2.port=18082 -Dmw-service2.host=${HOST} \
		-Dmw-service3.name=rmFlight -Dmw-service3.port=18083 -Dmw-service3.host=${HOST} \
		-Dmw-service4.name=rmCustomer -Dmw-service4.port=18084 -Dmw-service4.host=${HOST}