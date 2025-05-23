package com.zxylearn.eleserver.service.Impl;

import com.zxylearn.eleserver.pojo.EleBusiness;
import com.zxylearn.eleserver.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Autowired
    private ElasticsearchOperations esOps;

    @Override
    public boolean createDoc(String id) {
        if (esOps.exists(id, EleBusiness.class)) {
            return false;
        }

        EleBusiness eleBusiness = new EleBusiness();
        eleBusiness.setId(id);
        eleBusiness.setStoreItems(new ArrayList<>());
        esOps.save(eleBusiness);
        return true;
    }

    @Override
    public boolean updateStoreName(String id, String storeName) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        eleBusiness.setStoreName(storeName);
        esOps.save(eleBusiness);
        return true;
    }

    @Override
    public boolean updateStoreDescription(String id, String storeDescription) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        eleBusiness.setStoreDescription(storeDescription);
        esOps.save(eleBusiness);
        return true;
    }

    @Override
    public boolean updateStoreVolume(String id, Integer storeVolume) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        eleBusiness.setStoreVolume(storeVolume);
        esOps.save(eleBusiness);
        return true;
    }

    @Override
    public boolean addStoreItem(String id, String itemName, String itemDescription, float itemPrice) {
        if(itemPrice < 0) {
            return false;
        }
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        EleBusiness.StoreItem storeItem = new EleBusiness.StoreItem(itemName, itemDescription, itemPrice);
        List<EleBusiness.StoreItem> storeItems = eleBusiness.getStoreItems();
        if(storeItems == null) {
            return false;
        }
        storeItems.add(storeItem);
        esOps.save(eleBusiness);
        return true;
    }

    @Override
    public boolean delStoreItem(String id, String itemName) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        List<EleBusiness.StoreItem> storeItems = eleBusiness.getStoreItems();
        for (EleBusiness.StoreItem item : storeItems) {
            if (item.getItemName().equals(itemName)) {
                storeItems.remove(item);
                esOps.save(eleBusiness);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateLocation(String id, double longitude, double latitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return false;
        }

        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }

        Point location = new Point(latitude, longitude);
        eleBusiness.setLocation(location);
        esOps.save(eleBusiness);
        return true;
    }

}
