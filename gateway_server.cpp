#include <iostream>
#include <httplib.h>
#include <spdlog/spdlog.h>

const uint16_t gateway_server_port = 8080;


int main(int argc, char* argv[])
{
    spdlog::set_pattern("%^[%H:%M:%S gateway] [%L] %v%$");
    spdlog::set_level(spdlog::level::info);
    

    httplib::Server gate_server;

    gate_server.Get("/", [](const httplib::Request& request, httplib::Response& response)
    {
        response.set_content("Test!!!----", "text/plain");
    });

    // 响应验证码
    gate_server.Get("/captcha", [](const httplib::Request& request, httplib::Response& response)
    {
        const char* json_response = R"({
            "captchaBase64": "",
            "captchaId": "123456"
        })";

        response.set_content(json_response, "application/json");
    });


    spdlog::info("gateway_erver is running!");
    gate_server.listen("127.0.0.1", gateway_server_port);

    return 0;
}
