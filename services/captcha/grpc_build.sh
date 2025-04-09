#!/bin/bash


ACTION=$1

PROTO_FILE=service


if [ "$ACTION" == "clean" ]; then
  echo "Cleaning generated files..."
  rm -rf ${PROTO_FILE}.grpc.* ${PROTO_FILE}.pb.*
else
  echo "Generating gRPC and proto files..."
  protoc --cpp_out=. --grpc_out=. --plugin=protoc-gen-grpc=$(which grpc_cpp_plugin) $PROTO_FILE.proto
fi
