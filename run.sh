#!/bin/bash

java -jar /tsf-alauda.jar --server.port=$PORT --server.servlet.context-path=$CONTEXT --container.platform.alauda.endpoint=$ENDPOINT
