gateway_server:gateway_server.cpp
	g++ -std=c++17 -o $@ $^ -lspdlog -lfmt

clean:
	rm -rf gateway_server

.PHONY:clean