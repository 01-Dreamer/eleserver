
service = auth


all:$(service)_client $(service)_server
$(service)_client:$(service)_client.cpp $(service).pb.cc $(service).grpc.pb.cc
	g++ -w -std=c++17 -o $@ $^ -lgrpc++ -lprotobuf -lpthread -lredis++ -lhiredis

$(service)_server:$(service)_server.cpp $(service).pb.cc $(service).grpc.pb.cc
	g++ -w -std=c++17 -o $@ $^ -lgrpc++ -lprotobuf -lpthread -lredis++ -lhiredis -luuid -lb64\
	                           -I/usr/include/x86_64-linux-gnu/ImageMagick-6 -I/usr/include/ImageMagick-6 -lMagick++-6.Q16\

clean:
	rm -rf $(service)_client $(service)_server

.PHONY:clean
