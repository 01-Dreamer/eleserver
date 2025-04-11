#include <iostream>
#include <string>
#include <thread>
#include <chrono>
#include <nlohmann/json.hpp>
#include <sw/redis++/redis++.h>
#include "../../utils/uuid.h"
#include "../../utils/captcha.h"

#include <grpcpp/grpcpp.h>
#include "auth.grpc.pb.h"

const std::string auth_server_address = "127.0.0.1:50051";
const std::string captcha_image_list_name = "captchaImageList";
sw::redis::Redis *redis15_client;

class AuthServiceImpl final : public AUTH_RPC::AuthService::Service
{

public:
    grpc::Status GetCaptchaImage(grpc::ServerContext *context,
                                 const AUTH_RPC::CaptchaImageRequest *request,
                                 AUTH_RPC::CaptchaImageResponse *response) override
    {
        // redis15中图像验证码存储量小于10时生成50个验证码
        if(redis15_client->llen(captcha_image_list_name) < 10)
        {
            std::thread t([](){

                for(int i = 0; i < 50; i++)
                {
                    nlohmann::json captcha_image_json;
                    std::string captcha_image_id, captcha_image_base64, captcha_image_value;

                    UUID::generate_uuid(captcha_image_id);
                    CAPTCHA::generate_captcha_image(6, captcha_image_base64, captcha_image_value);

                    captcha_image_json["captcha_image_id"] = captcha_image_id;
                    captcha_image_json["captcha_image_base64"] = captcha_image_base64;
                    captcha_image_json["captcha_image_value"] = captcha_image_value;

                    redis15_client->lpush(captcha_image_list_name, captcha_image_json.dump());
                }

            });
            t.detach();
        }

        std::string captcha_image_json_in_redis15 = *redis15_client->rpop(captcha_image_list_name);
        nlohmann::json j = nlohmann::json::parse(captcha_image_json_in_redis15);

        std::string captcha_image_id = j["captcha_image_id"];
        std::string captcha_image_base64 = j["captcha_image_base64"];
        std::string captcha_image_value = j["captcha_image_value"];

        // 验证码有效时间 120s
        redis15_client->set(captcha_image_id, captcha_image_value, std::chrono::seconds(120));

        response->set_captcha_image_id(captcha_image_id);
        response->set_captcha_image_base64(captcha_image_base64);


        return grpc::Status::OK;
    }

    grpc::Status VerifyCaptchaImage(grpc::ServerContext *context,
                                    const AUTH_RPC::CaptchaImageVerification *request,
                                    AUTH_RPC::Status *response) override
    {
        std::string captcha_image_id = request->captcha_image_id();
        std::string captcha_image_value = request->captcha_image_value();

        // 字母大小写转换由前端负责，后端不处理
        /*
        for(char &c:captcha_image_value)
           if('A' <= c && c <= 'Z')
              c += 32;
        */

        auto captcha_image_value_in_redis15_ptr = redis15_client->get(captcha_image_id);
        if(captcha_image_value_in_redis15_ptr)
        {
            std::string captcha_image_id_in_redis15 = *captcha_image_value_in_redis15_ptr;
            response->set_status(captcha_image_id_in_redis15 == captcha_image_value);
        }
        else response->set_status(false);


        return grpc::Status::OK;
    }

}; // class AuthServiceImpl

bool init_redis15()
{
    try
    {
        sw::redis::ConnectionOptions opts;
        opts.host = "127.0.0.1";
        opts.port = 6379;
        opts.db = 15;
        opts.keep_alive = true;
        redis15_client = new sw::redis::Redis(opts);
        std::cout << "success to init redis-15" << std::endl;
        return true;
    }
    catch (const std::exception &e)
    {
        std::cerr << "failed to init redis-15:" << e.what() << std::endl;
        return false;
    }
}

void run_auth_server()
{
    AuthServiceImpl service;

    grpc::ServerBuilder builder;
    builder.AddListeningPort(auth_server_address, grpc::InsecureServerCredentials());
    builder.RegisterService(&service);

    std::unique_ptr<grpc::Server> server(builder.BuildAndStart());
    std::cout << "auth_server is running on " << auth_server_address << std::endl;
    server->Wait();
}

int main()
{
    if(!init_redis15()) return -1;
    run_auth_server();

    delete redis15_client;


    return 0;
}