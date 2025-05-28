package com.zxylearn.eleserver.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxylearn.eleserver.service.AmapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class AmapServiceImpl implements AmapService {

    private final String amapkeySecret;

    public AmapServiceImpl(@Value("${amap.KeySecret}") String amapkeySecret) {
        this.amapkeySecret = amapkeySecret;
    }

    public String getFormattedAddress(double longitude, double latitude) {
        String url = "https://restapi.amap.com/v3/geocode/regeo?output=json&location=%f,%f&key=" + amapkeySecret;

        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = String.format(url, longitude, latitude);
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

    public Map<String, Double> getPosition(String address) {
        String url = "https://restapi.amap.com/v3/geocode/geo?address=%s&output=json&key=" + amapkeySecret;

        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = String.format(url, address);
        ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
        String responseBody = response.getBody();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            JsonNode geocodesNode = rootNode.path("geocodes");
            if (geocodesNode.isArray() && !geocodesNode.isEmpty()) {
                JsonNode firstGeoNode = geocodesNode.get(0);
                String location = firstGeoNode.path("location").asText(); // 获取经纬度信息，格式为 "longitude,latitude"
                String[] coordinates = location.split(",");

                if (coordinates.length == 2) {
                    double longitude = Double.parseDouble(coordinates[0]);
                    double latitude = Double.parseDouble(coordinates[1]);
                    return Map.of("longitude", longitude, "latitude", latitude);
                }
            }
        } catch (Exception e) {
            log.warn("failed to get location for address '{}': {}", address, e.getMessage());
        }
        return null;
    }


    @Override
    public Map<String, Double> getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        String urlTemplate = "https://restapi.amap.com/v3/direction/driving?origin=%f,%f&destination=%f,%f&output=json&key=" + amapkeySecret;
        String requestUrl = String.format(urlTemplate, longitude1, latitude1, longitude2, latitude2);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
        String responseBody = response.getBody();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            if (!"1".equals(rootNode.path("status").asText())) {
                log.warn("amap API returned error: {}", rootNode.path("info").asText());
                return null;
            }

            JsonNode paths = rootNode.path("route").path("paths");
            if (paths.isArray() && !paths.isEmpty()) {
                JsonNode firstPath = paths.get(0);
                double distance = firstPath.path("distance").asDouble();
                double duration = firstPath.path("duration").asDouble();
                return Map.of("distance", distance, "duration", duration);
            }
        } catch (Exception e) {
            log.warn("failed to get distance: {}", e.getMessage());
        }

        return null;
    }

}
