#!/bin/bash

#8080 rm

#ports numbered successively from middleware port

xterm -e ant server -Dserver-service.name=rmCar -Dserver-service.port=18081 &
xterm -e ant server -Dserver-service.name=rmRoom -Dserver-service.port=18082 &
xterm -e ant server -Dserver-service.name=rmFlight -Dserver-service.port=18083 &

#build but don't run the 3 clients for the middleware




# ant client -Dclient-service.name=rmMW -Dclient-service.port=18080