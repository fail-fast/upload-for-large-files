#!/bin/bash

curl -X POST -v -i --data-binary @shakespeare.txt -H "Transfer-Encoding: chunked" http://localhost:8080/file-upload
