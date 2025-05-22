package com.zxylearn.eleserver.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxylearn.eleserver.service.AmapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AmapServiceImpl implements AmapService {

    private final String URL;

    public AmapServiceImpl(@Value("${amap.KeySecret}") String amapkeySecret) {
        URL = "https://restapi.amap.com/v3/geocode/regeo?output=json&location=%f,%f&key=" + amapkeySecret;
    }

    public String getFormattedAddress(double longitude, double latitude) {
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = String.format(URL, longitude, latitude);
        ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
        String responseBody = response.getBody();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            JsonNode regeocodeNode = rootNode.path("regeocode");
            String formattedAddress = regeocodeNode.path("formatted_address").asText();

            return formattedAddress.isEmpty() ? null : formattedAddress;

        } catch (Exception e) {
            log.warn("failed to get formatted address: {}", e.getMessage());
            return null;
        }
    }
}
