all:captcha_client captcha_server

captcha_client:captcha_client.cpp captcha.pb.cc captcha.grpc.pb.cc
	g++ -w -std=c++17 -o $@ $^ -lgrpc++ -lprotobuf -lpthread -lredis++ -lhiredis

captcha_server:captcha_server.cpp captcha.pb.cc captcha.grpc.pb.cc
	g++ -w -std=c++17 -o $@ $^ -lgrpc++ -lprotobuf -lpthread -lredis++ -lhiredis\
	                        -I/usr/include/x86_64-linux-gnu/ImageMagick-6 -I/usr/include/ImageMagick-6 -lMagick++-6.Q16 -lb64\
							-luuid

clean:
	rm -rf captcha_client captcha_server

.PHONY:clean
