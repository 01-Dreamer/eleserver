#include <iostream>

#include <grpcpp/grpcpp.h>
#include "captcha.grpc.pb.h"

const std::string captcha_server_address = "127.0.0.1:50051";
CAPTCHA_RPC::Captcha::Stub* captcha_stub;


bool InitClient()
{
    captcha_stub = new CAPTCHA_RPC::Captcha::Stub(grpc::CreateChannel(captcha_server_address, grpc::InsecureChannelCredentials()));
}


bool GetCaptcha(std::string* captchaJson)
{
    grpc::ClientContext context;
    CAPTCHA_RPC::Empty request;
    CAPTCHA_RPC::CaptchaJson response;

    grpc::Status status = captcha_stub->GetCaptcha(&context, request, &response);
    if(status.ok())
    {
        //std::cout << response.captcha_json() << std::endl;
        *captchaJson = response.captcha_json();
        return true;
    }
    else
    {
        std::cout << "RPC GetCaptcha failed!" << std::endl;
        return false;
    }
}


bool VerifyCaptcha(const std::string& captchaJson, bool* pass)
{
    grpc::ClientContext context;
    CAPTCHA_RPC::CaptchaJson request;
    CAPTCHA_RPC::Status response;

    request.set_captcha_json("{\"captchaId\":\"9013c7d4-0629-473e-ab75-64b1535499df\",\"captchaValue\":\"VbPpW4\"}");

    grpc::Status status = captcha_stub->VerifyCaptcha(&context, request, &response);
    if(status.ok())
    {
        std::cout << response.status() << std::endl;
        *pass = response.status();
        return true;
    }
    else
    {
        std::cout << "RPC VerifyCaptcha failed!" << std::endl;
        return false;
    }
}


int main()
{
    InitClient();

    std::string s;
    GetCaptcha(&s);

    bool pass;
    VerifyCaptcha("Test",&pass);



    delete captcha_stub;


    return 0;
}