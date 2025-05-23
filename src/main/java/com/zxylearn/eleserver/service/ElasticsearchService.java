package com.zxylearn.eleserver.service;

public interface ElasticsearchService {
    boolean createDoc(String id);
    boolean updateStoreName(String id, String storeName);
    boolean updateStoreDescription(String id, String storeDescription);
    boolean updateStoreVolume(String id, Integer storeVolume);
    boolean addStoreItem(String id, String itemName, String itemDescription, float itemPrice);
    boolean delStoreItem(String id, String itemName);
    boolean updateLocation(String id, double longitude, double latitude);
}
