#include <iostream>

#include <grpcpp/grpcpp.h>
#include "auth.grpc.pb.h"

const std::string captcha_server_address = "127.0.0.1:50051";
AUTH_RPC::AuthService::Stub* auth_stub;


bool InitClient()
{
    auth_stub = new AUTH_RPC::AuthService::Stub(grpc::CreateChannel(captcha_server_address, grpc::InsecureChannelCredentials()));
}


bool GetCaptchaImage()
{
    grpc::ClientContext context;
    AUTH_RPC::CaptchaImageRequest request;
    AUTH_RPC::CaptchaImageResponse response;

    grpc::Status status = auth_stub->GetCaptchaImage(&context, request, &response);
    if(status.ok())
    {
        //std::cout << response.captcha_image_id() << std::endl;
        //std::cout << response.captcha_image_base64() << std::endl;
        return true;
    }
    else
    {
        std::cout << "RPC GetCaptchaImage failed!" << std::endl;
        return false;
    }
}


bool VerifyCaptchaImage()
{
    grpc::ClientContext context;
    AUTH_RPC::CaptchaImageVerification request;
    AUTH_RPC::Status response;

    grpc::Status status = auth_stub->VerifyCaptchaImage(&context, request, &response);
    if(status.ok())
    {
        std::cout << response.status() << std::endl;
        return true;
    }
    else
    {
        std::cout << "RPC VerifyCaptchaImage failed!" << std::endl;
        return false;
    }
}


int main()
{
    InitClient();


    GetCaptchaImage();
    VerifyCaptchaImage();


    delete auth_stub;


    return 0;
}