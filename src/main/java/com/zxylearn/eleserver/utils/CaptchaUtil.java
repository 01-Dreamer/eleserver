package com.zxylearn.eleserver.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;


public class CaptchaUtil {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    public static final char[] IMG_CAPTCHA_CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    public static final char[] EMAIL_CAPTCHA_CHAR = "0123456789".toCharArray();

    public static BufferedImage generateCaptchaImage(String captchaText) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        Font font = new Font("Microsoft YaHei", Font.BOLD, 24);
        g.setFont(font);

        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            g.setColor(getRandomColor());
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }

        int textWidth = g.getFontMetrics().stringWidth(captchaText);
        int startX = (WIDTH - textWidth) / 2;

        startX = Math.max(10, startX);

        for (int i = 0; i < captchaText.length(); i++) {
            char c = captchaText.charAt(i);
            int charWidth = g.getFontMetrics().charWidth(c);
            int y = 28 + random.nextInt(5) - 2;

            g.setColor(getRandomColor());
            g.drawString(String.valueOf(c), startX, y);
            startX += charWidth;
        }

        for (int i = 0; i < 30; i++) {
            g.setColor(getRandomColor());
            g.drawOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 1, 1);
        }

        g.dispose();
        return image;
    }

    private static Color getRandomColor() {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public static String generateRandomCaptchaText(char[] CAPTCHA_CHAR) {
        StringBuilder captcha = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            captcha.append(CAPTCHA_CHAR[random.nextInt(CAPTCHA_CHAR.length)]);
        }
        return captcha.toString();
    }

}