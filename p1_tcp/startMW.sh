#!/bin/bash


# ant build-middleware-client -Dclient-service.name=rmCar -Dclient-service.port=18081
# ant build-middleware-client -Dclient-service.name=rmRoom -Dclient-service.port=18082
# ant build-middleware-client -Dclient-service.name=rmFlight -Dclient-service.port=18083 
# ant build-middleware-client -Dclient-service.name=rmCustomer -Dclient-service.port=18084 

ant middleware -Dserver-service.name=rmMW -Dserver-service.port=18080 \
		-Dmw-service1.name=rmCar -Dmw-service1.port=18081 -Dmw-service1.host=lab1-14 \
		-Dmw-service2.name=rmRoom -Dmw-service2.port=18082 -Dmw-service2.host=lab1-14 \
		-Dmw-service3.name=rmFlight -Dmw-service3.port=18083 -Dmw-service3.host=lab1-14 \
		-Dmw-service4.name=rmCustomer -Dmw-service4.port=18084 -Dmw-service4.host=lab1-14