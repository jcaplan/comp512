#!/bin/bash

# clientMain=client.Client
clientMain=client.SingleClientPerformanceTest


ant client -Dmiddleware-service.host=localhost -Dclient-main=${clientMain}
