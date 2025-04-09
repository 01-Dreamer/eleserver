gateway_server:gateway_server.cpp
	g++ -w -std=c++17 -o $@ $^ -lspdlog -lfmt -I/usr/include/x86_64-linux-gnu/ImageMagick-6 -I/usr/include/ImageMagick-6 -lMagick++-6.Q16 -lb64

clean:
	rm -rf gateway_server

.PHONY:clean