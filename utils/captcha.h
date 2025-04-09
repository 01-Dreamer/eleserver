#ifndef CAPTCHA_H
#define CAPTCHA_H

#include <Magick++.h>
#include <iostream>
#include <ctime>
#include <cstdlib>
#include <cstring>
#include "base64.h"

namespace CAPTCHA
{
    // 生成验证码
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
    bool generate_captcha(int len, std::string& outputImage, std::string& outputValue)
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
        image.write(&blob, "PNG");// .png格式

        std::vector<unsigned char> imageData(blob.length());
        std::memcpy(imageData.data(), blob.data(), blob.length());

        std::string imageStr(imageData.begin(), imageData.end());
        BASE64::encode(&imageStr, outputImage);

        // 转小写字母
        for(char& c:outputValue)
           if('A' <= c && c <= 'Z')
              c += 32;
        
        return true;
    }

} // namespace CAPTCHA


#endif // CAPTCHA_H