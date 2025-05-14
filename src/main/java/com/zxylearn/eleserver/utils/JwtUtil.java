package com.zxylearn.eleserver.utils;

import io.jsonwebtoken.*;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {

    private static final String SECRET =
            "s4Sylqsp/keMVR4LnQnNCSTOMKK82/crJ3VpEpldQpZUzGEvB1JAga3Wv6HDASL7" +
            "NQJLVnnnD+xw1hlukmgdSq4rZu2BJNsXnK4ghMLcoFePmS7zKVtdhiIx+CNFU5/1" +
            "krr9eP04FwxrHQWmsK93aLPzgVp2HKgNbFCpjXi9JbC9Rs2Ncf0GU6paBM1uI9B/" +
            "5hltIVo57iRgyx1ZlXi4itzcoAlsChHokgL4rdDzpJKo0F1E06FIJm8cgcjpM4+g" +
            "sVz+Brz/i/Dg4Coyyc7FW4+qWbNyGP8BsbutlJGDXMnkCCa1q2HaBELhe4t8sfi/" +
            "5mhs0kzyLm6zENNgG8AnyA==zxylearn+ele";

    private static final Key SECRET_KEY = new SecretKeySpec(
            SECRET.getBytes(StandardCharsets.UTF_8),
            SignatureAlgorithm.HS256.getJcaName()
    );

    private static final long REFRESH_EXPIRATION = 60 * 60 * 1000;
    private static final long ACCESS_EXPIRATION = 5 * 60 * 1000;
    public static final String REFRESH = "refresh";
    public static final String ACCESS = "access";

    public static String generateToken(Integer userId, String email, String jwtType) {
        long expirationTimeMillis = 0;
        if(jwtType.equals(REFRESH)) {
            expirationTimeMillis = REFRESH_EXPIRATION;
        }
        else if(jwtType.equals(ACCESS)) {
            expirationTimeMillis = ACCESS_EXPIRATION;
        } else {
            return null;
        }

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTimeMillis);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("type", jwtType)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String getTokenId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getId();
    }

    public static Integer getUserId(String token) {
        return Integer.parseInt(Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }

    public static String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    public static boolean verifyToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}
