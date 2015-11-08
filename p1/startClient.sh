#!/bin/bash

clientMain=client.Client
# clientMain=client.PerformanceTest


ant client -Dmiddleware-service.host=localhost -Dclient-main=${clientMain}
