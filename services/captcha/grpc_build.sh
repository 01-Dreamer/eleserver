#!/bin/bash

ACTION=$1

PROTO_FILE=captcha


if [ "$ACTION" == "clean" ]; then
  echo "Cleaning gRPC files..."
  rm -rf ${PROTO_FILE}.grpc.* ${PROTO_FILE}.pb.*
else
  echo "Generating gRPC files..."
  protoc --cpp_out=. --grpc_out=. --plugin=protoc-gen-grpc=$(which grpc_cpp_plugin) $PROTO_FILE.proto
fi
