#!/bin/bash

clientMain=client.Client
#clientMain=client.MultipleClientsPerformanceTest


ant client -Dmiddleware-service.host=localhost -Dclient-main=${clientMain}
