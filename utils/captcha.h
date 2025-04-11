#ifndef CAPTCHA_H
#define CAPTCHA_H

#include <Magick++.h>
#include <curl/curl.h>
#include <iostream>
#include <ctime>
#include <cstdlib>
#include <random>
#include <cstring>
#include "base64.h"


namespace CAPTCHA {
    // 生成验证码文本
    bool generateCaptchaText(int len, std::string &captchaText)
    {
        static std::string chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        std::srand(std::time(nullptr));

        for (int i = 0; i < len; i++)
        {
            int randIndex = std::rand() % chars.size();
            captchaText += chars[randIndex];
        }

        return true;
    }

    // 添加干扰线
    void addNoise(Magick::Image &image)
    {
        int width = image.columns();
        int height = image.rows();
        std::srand(std::time(0));

        for (int i = 0; i < 15; i++) // 干扰线条数
        {
            int x1 = std::rand() % width;
            int y1 = std::rand() % height;
            int x2 = std::rand() % width;
            int y2 = std::rand() % height;

            image.strokeColor(Magick::Color("gray"));
            image.strokeWidth(1);
            image.draw(Magick::DrawableLine(x1, y1, x2, y2));
        }
    }

    // 生成随机背景颜色
    void generateRandomBackground(Magick::Image &image)
    {
        int width = image.columns();
        int height = image.rows();
        std::srand(std::time(0));

        int r1 = std::rand() % 256;
        int g1 = std::rand() % 256;
        int b1 = std::rand() % 256;
        image.fillColor(Magick::ColorRGB(r1 / 255.0, g1 / 255.0, b1 / 255.0));
        image.opaque(Magick::Color("white"), image.fillColor());

        for (int i = 0; i < 5; i++) // 颜色块个数
        {
            int r = std::rand() % 256;
            int g = std::rand() % 256;
            int b = std::rand() % 256;
            int x = std::rand() % width;
            int y = std::rand() % height;
            int radius = std::rand() % 50 + 20;

            image.fillColor(Magick::ColorRGB(r / 255.0, g / 255.0, b / 255.0));
            image.draw(Magick::DrawableEllipse(x, y, radius, radius, 0, 360));
        }

        image.blur(5.0, 1.5);
    }

    // 生成验证码图片
    bool generate_captcha_image(int len, std::string &outputBase64, std::string &outputValue)
    {
        generateCaptchaText(len, outputValue);

        // 图像大小
        int width = 300;
        int height = 100;

        Magick::Image image(Magick::Geometry(width, height), Magick::Color("white"));
        generateRandomBackground(image);

        image.font("Helvetica");
        image.fillColor(Magick::Color("black"));
        image.strokeColor(Magick::Color("transparent"));
        image.strokeWidth(0);

        // 字体大小
        int fontSize = height / 2;
        image.fontPointsize(fontSize);

        int xOffset = 20;
        int yOffset = height / 1.5;
        std::srand(std::time(0));

        for (char c : outputValue)
        {
            std::string singleChar(1, c);

            Magick::Image charImage(Magick::Geometry(50, height), Magick::Color("white"));
            charImage.font("Helvetica");
            charImage.fillColor(Magick::Color("black"));
            charImage.fontPointsize(fontSize);

            charImage.draw(Magick::DrawableText(0, yOffset, singleChar));

            // 随机倾斜
            double shearX = (std::rand() % 14 - 7);
            charImage.shear(shearX, 0);

            // 波浪扭曲
            charImage.wave(3.0, 25.0);

            // 将字符图像叠加到主图像
            image.composite(charImage, xOffset, 0, Magick::OverCompositeOp);

            // 间距
            xOffset += fontSize * 0.8;
        }

        addNoise(image);

        Magick::Blob blob;
        image.write(&blob, "PNG"); // .png格式

        std::vector<unsigned char> imageData(blob.length());
        std::memcpy(imageData.data(), blob.data(), blob.length());

        std::string imageStr(imageData.begin(), imageData.end());
        BASE64::encode(&imageStr, outputBase64);

        // 大写字母转小写字母
        for (char &c : outputValue)
            if ('A' <= c && c <= 'Z')
                c += 32;

        return true;
    }

    // 生成纯数字验证码
    std::string generate_verification_code(int len)
    {
        std::random_device rd;
        std::mt19937 gen(rd());
        std::uniform_int_distribution<> dis(0, 9);

        std::stringstream ss;
        for (int i = 0; i < len; ++i)
        {
            ss << dis(gen);
        }
        return ss.str();
    }

    // libcurl回调函数
    static size_t payload_source(void *ptr, size_t size, size_t nmemb, void *userp)
    {
        std::string *data = (std::string *)userp;
        if (size * nmemb < 1 || data->empty())
            return 0;

        size_t len = data->copy((char *)ptr, size * nmemb);
        *data = data->substr(len);
        return len;
    }

    // 发送邮箱验证码
    bool send_email(const std::string *to, std::string &verification_code)
    {
        verification_code = generate_verification_code(6);

        CURL *curl;
        CURLcode res = CURLE_OK;

        const std::string from = std::getenv("MY_QQ_EMAIL");
        const std::string smtp_server = "smtp.qq.com";
        const int smtp_port = 465;
        const std::string auth_code = std::getenv("QQ_EMAIL_AUTH");

        const std::string subject = "软件工程实训饿了么验证码";
        const std::string body = "您的验证码是: " + verification_code + "\n验证码有效时间为120秒";
        std::string email_data =
            "From: " + from + "\r\n"
                              "To: " +
            *to + "\r\n"
                 "Subject: " +
            subject + "\r\n"
                      "MIME-Version: 1.0\r\n"
                      "Content-Type: text/plain; charset=utf-8\r\n"
                      "\r\n" +
            body + "\r\n";

        curl = curl_easy_init();
        if (curl)
        {
            std::string url = "smtps://" + smtp_server + ":" + std::to_string(smtp_port);
            curl_easy_setopt(curl, CURLOPT_URL, url.c_str());

            curl_easy_setopt(curl, CURLOPT_USERNAME, from.c_str());
            curl_easy_setopt(curl, CURLOPT_PASSWORD, auth_code.c_str());
            curl_easy_setopt(curl, CURLOPT_LOGIN_OPTIONS, "AUTH=LOGIN");
            curl_easy_setopt(curl, CURLOPT_MAIL_FROM, from.c_str());
            struct curl_slist *recipients = NULL;
            recipients = curl_slist_append(recipients, (*to).c_str());
            curl_easy_setopt(curl, CURLOPT_MAIL_RCPT, recipients);
            curl_easy_setopt(curl, CURLOPT_READFUNCTION, payload_source);
            curl_easy_setopt(curl, CURLOPT_READDATA, &email_data);
            curl_easy_setopt(curl, CURLOPT_UPLOAD, 1L);
            curl_easy_setopt(curl, CURLOPT_USE_SSL, CURLUSESSL_ALL);
            curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
            curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0L);


            res = curl_easy_perform(curl);

            curl_slist_free_all(recipients);
            curl_easy_cleanup(curl);

            if (res != CURLE_OK)
            {
                std::cerr << "failed to send email:" << curl_easy_strerror(res) << std::endl;
                return false;
            }
            return true;
        }
        return false;
    }


} // namespace CAPTCHA

#endif // CAPTCHA_H