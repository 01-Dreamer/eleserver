package com.zxylearn.eleserver.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {

    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;

    private final StringRedisTemplate stringRedisTemplateCaptcha;
    private final StringRedisTemplate stringRedisTemplateJwt;

    public static final int CAPTCHA_REDIS = 0;
    public static final int JWT_REDIS = 1;

    public RedisUtil(@Value("${spring.data.redis.host}") String redisHost,
                     @Value("${spring.data.redis.port}") int redisPort,
                     @Value("${spring.data.redis.password}") String redisPassword) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisPassword = redisPassword;
        this.stringRedisTemplateCaptcha = createRedisTemplate(CAPTCHA_REDIS);
        this.stringRedisTemplateJwt = createRedisTemplate(JWT_REDIS);
    }

    private StringRedisTemplate createRedisTemplate(int redisIndex) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setPassword(redisPassword);
        config.setDatabase(redisIndex);

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        return new StringRedisTemplate(factory);
    }

    private StringRedisTemplate getRedisTemplate(int redisType) {
        if (redisType == CAPTCHA_REDIS) {
            return stringRedisTemplateCaptcha;
        } else if (redisType == JWT_REDIS) {
            return stringRedisTemplateJwt;
        } else {
            log.error("error redisType:{}", redisType);
            return stringRedisTemplateCaptcha;
        }
    }

    public boolean set(String key, String value, long timeout, int redisType) {
        if (key == null || value == null || timeout <= 0) {
            return false;
        }

        getRedisTemplate(redisType).opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
        return true;
    }

    public boolean set(String key, String value, int redisType) {
        if (key == null || value == null) {
            return false;
        }

        getRedisTemplate(redisType).opsForValue().set(key, value);
        return true;
    }

    public String get(String key, int redisType) {
        return getRedisTemplate(redisType).opsForValue().get(key);
    }

    public boolean exists(String key, int redisType) {
        return Boolean.TRUE.equals(getRedisTemplate(redisType).hasKey(key));
    }

    public boolean del(String key, int redisType) {
        return Boolean.TRUE.equals(getRedisTemplate(redisType).delete(key));
    }

    public long ttl(String key, int redisType) {
        return getRedisTemplate(redisType).getExpire(key, TimeUnit.SECONDS);
    }

}
