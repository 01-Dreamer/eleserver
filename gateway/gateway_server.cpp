#include <iostream>
#include <httplib.h>
#include <spdlog/spdlog.h>

const uint16_t gateway_server_port = 8080;


bool InitLog();
bool InitServer(httplib::Server*);


//////////////////////////////////////////////////////////////
// main
//////////////////////////////////////////////////////////////
int main(int argc, char* argv[])
{
    InitLog();

    httplib::Server gate_server;
    InitServer(&gate_server);

    spdlog::info("gateway_erver is running!");
    gate_server.listen("127.0.0.1", gateway_server_port);


    return 0;
}
//////////////////////////////////////////////////////////////
// main
//////////////////////////////////////////////////////////////


// 初始化日志
bool InitLog()
{
    spdlog::set_pattern("%^[%H:%M:%S gateway] [%L] %v%$");
    spdlog::set_level(spdlog::level::info);

    
    return true;
}


// 初始化服务器
bool InitServer(httplib::Server* server)
{
    
    server->Get("/", [](const httplib::Request& request, httplib::Response& response)
    {
        response.set_content("Test!!!----", "text/plain");
    });

    // 验证码
    server->Get("/captcha", [](const httplib::Request& request, httplib::Response& response)
    {

        response.set_content("Test!!!----", "application/json");
    });

    // 登录
    server->Post("/login", [](const httplib::Request& request, httplib::Response& response)
    {

        response.set_content("Test!!!----", "application/json");
    });

    // 注册
    server->Post("/register", [](const httplib::Request& request, httplib::Response& response)
    {

        response.set_content("Test!!!----", "application/json");
    });


    return true;
}