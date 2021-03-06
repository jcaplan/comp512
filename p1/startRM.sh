#!/bin/bash


#ports numbered successively from middleware port 18080

xterm -e ant server -Dserver-service.name=rmCar -Dserver-service.port=28081 \
	-Dserver-shutdown.port=28085 &
sleep 5
xterm -e ant server -Dserver-service.name=rmRoom -Dserver-service.port=28082 \
	-Dserver-shutdown.port=28087 &
sleep 5
xterm -e ant server -Dserver-service.name=rmFlight -Dserver-service.port=28083 \
	-Dserver-shutdown.port=28086 &
sleep 5
xterm -e ant server -Dserver-service.name=rmCustomer -Dserver-service.port=28084 \
	-Dserver-shutdown.port=28088 &

# xterm -e ant server -Dserver-service.name=rmRoom -Dserver-service.port=18082 &
# sleep 2
# xterm -e ant server -Dserver-service.name=rmFlight -Dserver-service.port=18083 &
# sleep 2
# xterm -e ant server -Dserver-service.name=rmCustomer -Dserver-service.port=18084 &
