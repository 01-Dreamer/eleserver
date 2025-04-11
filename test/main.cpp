#include <iostream>
#include "../utils/captcha.h"


int main(int argc, char *argv[])
{
    std::string to_email = "2711339704@qq.com";
    std::string verification_code;

    if(CAPTCHA::send_email(&to_email, verification_code)) {
        std::cout << "生成的验证码: " << verification_code << std::endl;
        std::cout << "邮件发送成功！"<< std::endl;
    } else {
        std::cerr << "发送邮件失败！" << std::endl;
    }




    return 0;
}
