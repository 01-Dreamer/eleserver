#include "../utils/generate_captcha.h"
#include <fstream>  // 用于文件操作
#include <iostream>

int main(int argc, char *argv[])
{
    std::string str;
    GENERATE_CAPTCHA::generate_captcha(6, str);

	std::cout << str;

	return 0;

    // 打开文件并写入验证码内容
    std::ofstream outFile("captcha.txt");
    if (outFile.is_open()) {
        outFile << str;  // 将验证码写入文件
        outFile.close();  // 关闭文件
        std::cout << "Captcha saved to captcha.txt" << std::endl;
    } else {
        std::cerr << "Error opening file for writing!" << std::endl;
    }

    return 0;
}
