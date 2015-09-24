#!/bin/bash


#ports numbered successively from middleware port 18080

xterm -e ant server -Dserver-service.name=rmCar -Dserver-service.port=18081 &
xterm -e ant server -Dserver-service.name=rmRoom -Dserver-service.port=18082 &
xterm -e ant server -Dserver-service.name=rmFlight -Dserver-service.port=18083 &
