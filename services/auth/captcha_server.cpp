#include <iostream>
#include <string>
#include <thread>
#include <chrono>
#include <nlohmann/json.hpp>
#include <sw/redis++/redis++.h>
#include "../../utils/captcha.h"
#include "../../utils/uuid.h"

#include <grpcpp/grpcpp.h>
#include "captcha.grpc.pb.h"

const std::string captcha_server_address = "127.0.0.1:50051";
sw::redis::Redis *redis15_client;


class CaptchaServiceImpl final : public CAPTCHA_RPC::Captcha::Service {

public:
    grpc::Status GetCaptcha(grpc::ServerContext *context, const CAPTCHA_RPC::Empty *request, CAPTCHA_RPC::CaptchaJson *response) override
    {
        if(redis15_client->llen("captchaList") < 10 )
        {
            std::thread t([](){

                for(int i = 0; i < 50; i ++)
                {
                    nlohmann::json captchaJson;

                    std::string captchaBase64, captchaValue;
                    CAPTCHA::generate_captcha(6, captchaBase64, captchaValue);
                    captchaJson["captchaBase64"] = captchaBase64;
                    captchaJson["captchaValue"] = captchaValue;

                    std::string ID;
                    UUID::generate_uuid(ID);
                    captchaJson["captchaId"] = ID;

                    redis15_client->lpush("captchaList", captchaJson.dump());
                }

            });
            t.detach();
        }

        nlohmann::json j = nlohmann::json::parse(*redis15_client->rpop("captchaList"));

        std::string captchaId = j["captchaId"];
        std::string captchaValue = j["captchaValue"];
        redis15_client->set(captchaId, captchaValue, std::chrono::seconds(60));

        nlohmann::json captchaJson;
        captchaJson["captchaBase64"] = j["captchaBase64"];
        captchaJson["captchaId"] = j["captchaId"];
        response->set_captcha_json(captchaJson.dump());


        return grpc::Status::OK;
    }

    grpc::Status VerifyCaptcha(grpc::ServerContext *context, const CAPTCHA_RPC::CaptchaJson *request, CAPTCHA_RPC::Status *response) override
    {
        std::string captchaId, captchaValue;
        try {
            nlohmann::json j = nlohmann::json::parse(request->captcha_json());
            captchaId = j["captchaId"];
            captchaValue = j["captchaValue"];
        } catch (const nlohmann::json::parse_error& e) {
            response->set_status(false);
            return grpc::Status::OK;
        }
        
        auto valueInRedis15 = redis15_client->get(captchaId);
        if(!valueInRedis15)
        {
            response->set_status(false);
            return grpc::Status::OK;
        }
        
        std::string captchaValueInRedis15 = *valueInRedis15;

        // 大小写由前端转换，后端不处理
        /*
        for(char& c:captchaValue)
           if('A' <= c && c <= 'Z')
              c += 32;
        */

        response->set_status(captchaValue == captchaValueInRedis15);

        return grpc::Status::OK;
    }

}; // class CaptchaServiceImpl

void run_captcha_server()
{
    CaptchaServiceImpl service;

    grpc::ServerBuilder builder;
    builder.AddListeningPort(captcha_server_address, grpc::InsecureServerCredentials());
    builder.RegisterService(&service);

    std::unique_ptr<grpc::Server> server(builder.BuildAndStart());
    std::cout << "captcha_server is running on " << captcha_server_address << std::endl;
    server->Wait();
}

bool InitRedis()
{
    try
    {
        sw::redis::ConnectionOptions opts;
        opts.host = "127.0.0.1";
        opts.port = 6379;
        opts.db = 15;
        opts.keep_alive = true;
        redis15_client = new sw::redis::Redis(opts);
        return true;
    }
    catch (const std::exception &e)
    {
        std::cerr << "failed to init redis-15:" << e.what() << std::endl;
        return false;
    }
}

int main()
{
    InitRedis();
    run_captcha_server();

    delete redis15_client;

    
    return 0;
}
