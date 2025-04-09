server:server.cpp service.pb.cc service.grpc.pb.cc
	g++ -std=c++11 -o $@ $^ -lgrpc++ -lprotobuf -lpthread

clean:
	rm -rf server

.PHONY:clean
