#!/bin/bash


ant build-client -Dclient-service.name=rmCar -Dclient-service.port=18081
ant build-client -Dclient-service.name=rmRoom -Dclient-service.port=18082
ant build-client -Dclient-service.name=rmFlight -Dclient-service.port=18083 
ant middleware -Dserver-service.name=rmMW -Dserver-service.port=18080 \
		-Dmw-service1.name=rmCar -Dmw-service1.port=18081 \
		-Dmw-service2.name=rmRoom -Dmw-service2.port=18082 \
		-Dmw-service3.name=rmFlight -Dmw-service3.port=18083 