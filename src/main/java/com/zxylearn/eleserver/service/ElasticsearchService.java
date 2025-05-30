package com.zxylearn.eleserver.service;

import com.zxylearn.eleserver.pojo.EleBusiness;

import java.util.List;

public interface ElasticsearchService {
    boolean createDoc(String id);
    boolean updateStoreText(String id, String storeName, String storeDescription);
    boolean updateStoreCover(String id, String storeCover);
    boolean updateStoreVolume(String id, Integer storeVolume);
    boolean updateLocation(String id, Double longitude, Double latitude);
    boolean updateLocationText(String id, String locationText);
    boolean updateStoreItemCover(String id, Integer itemId, String storeItemCover);
    boolean updateStoreItemText(String id, Integer itemId, String itemName, String itemDescription, Float itemPrice);
    Integer addStoreItem(String id);
    boolean delStoreItem(String id, Integer itemId);
    String getStoreItemCover(String id, Integer itemId);
    EleBusiness getEleBusinessById(String id);
    List<EleBusiness> getAllEleBusiness();
    List<EleBusiness> getEleBusinessByKeyword(String Keyword);

}
