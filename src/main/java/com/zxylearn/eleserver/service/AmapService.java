package com.zxylearn.eleserver.service;

import java.util.Map;

public interface AmapService {
    String getFormattedAddress(double longitude, double latitude);
    Map<String, Double> getPosition(String address);
}
