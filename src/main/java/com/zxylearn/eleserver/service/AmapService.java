package com.zxylearn.eleserver.service;

import java.util.Map;

public interface AmapService {
    String getFormattedAddress(double longitude, double latitude);
    Map<String, Double> getPosition(String address);
    // 1 ----> 2
    Map<String, Double> getDistance(double longitude1, double latitude1, double longitude2, double latitude2);
}
