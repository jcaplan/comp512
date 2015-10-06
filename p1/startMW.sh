#!/bin/bash


ant build-middleware-client -Dserver-service.name=rmCar -Dserver-service.port=28081 \
-Dserver-service.host=lab1-14
# ant build-middleware-client -Dclient-service.name=rmRoom -Dclient-service.port=18082
# ant build-middleware-client -Dclient-service.name=rmFlight -Dclient-service.port=18083 
# ant build-middleware-client -Dclient-service.name=rmCustomer -Dclient-service.port=18084 

ant middleware -Dmiddleware-service.name=rmMW -Dmiddleware-service.port=28080 \
-Dserver-service.name=rmMW -Dserver-service.port=28080
#		-Dmw-service1.name=rmCar -Dmw-service1.port=18081 \
#		-Dmw-service2.name=rmRoom -Dmw-service2.port=18082 \
#		-Dmw-service3.name=rmFlight -Dmw-service3.port=18083 \
#		-Dmw-service4.name=rmCustomer -Dmw-service4.port=18084