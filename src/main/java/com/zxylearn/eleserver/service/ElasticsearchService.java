package com.zxylearn.eleserver.service;

import com.zxylearn.eleserver.pojo.EleBusiness;

import java.util.List;

public interface ElasticsearchService {
    boolean createDoc(String id);
    boolean updateStoreName(String id, String storeName);
    boolean updateStoreDescription(String id, String storeDescription);
    boolean updateStoreCover(String id, String storeCover);
    boolean updateStoreVolume(String id, Integer storeVolume);
    boolean updateLocation(String id, Double longitude, Double latitude);
    boolean updateLocationText(String id, String locationText);
    boolean addStoreItem(String id, String itemName, String itemDescription, String itemCover, Float itemPrice);
    boolean modifyStoreItem(String id, Integer itemId, String itemName, String itemDescription, String itemCover, Float itemPrice);
    boolean delStoreItem(String id, Integer itemId);
    EleBusiness getEleBusinessById(String id);
    List<EleBusiness> getAllEleBusiness();
    List<EleBusiness> getEleBusinessByKeyword(String Keyword);

}
