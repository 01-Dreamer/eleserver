package com.zxylearn.eleserver.service.Impl;


import com.zxylearn.eleserver.pojo.EleBusiness;
import com.zxylearn.eleserver.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        eleBusiness.setStoreName("商家" + id);
        eleBusiness.setStoreDescription("我是商家" + id);
        eleBusiness.setLocationText("云南大学呈贡校区软件学院");
        eleBusiness.setLocation(new Point(102.850549, 24.826936));
        eleBusiness.setStoreVolume(0);
        eleBusiness.setItemIndex(0);
        eleBusiness.setStoreItems(new ArrayList<>());
        esOps.save(eleBusiness);
        return true;
    }

    @Override
    public boolean updateStoreText(String id, String storeName, String storeDescription) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        eleBusiness.setStoreName(storeName);
        eleBusiness.setStoreDescription(storeDescription);
        esOps.save(eleBusiness);
        return true;
    }

    @Override
    public boolean updateStoreCover(String id, String storeCover) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        eleBusiness.setStoreCover(storeCover);
        esOps.save(eleBusiness);
        return true;
    }

    @Override
    public boolean addStoreVolume(String id) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        Integer newStoreVolume = eleBusiness.getStoreVolume() + 1;
        eleBusiness.setStoreVolume(newStoreVolume);
        esOps.save(eleBusiness);
        return true;
    }

    @Override
    public boolean updateLocation(String id, Double longitude, Double latitude, String locationText) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return false;
        }

        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }

        eleBusiness.setLocation(new Point(longitude, latitude));
        eleBusiness.setLocationText(locationText);
        esOps.save(eleBusiness);
        return true;
    }


    @Override
    public boolean updateStoreItemCover(String id, Integer itemId, String storeItemCover) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        List<EleBusiness.StoreItem> storeItems = eleBusiness.getStoreItems();
        for (EleBusiness.StoreItem item : storeItems) {
            if (item.getItemId().equals(itemId)) {
                item.setItemCover(storeItemCover);
                esOps.save(eleBusiness);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateStoreItemText(String id, Integer itemId, String itemName, String itemDescription, Float itemPrice) {
        if (itemPrice < 0) {
            return false;
        }
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        List<EleBusiness.StoreItem> storeItems = eleBusiness.getStoreItems();
        for (EleBusiness.StoreItem item : storeItems) {
            if (item.getItemId().equals(itemId)) {
                item.setItemName(itemName);
                item.setItemDescription(itemDescription);
                item.setItemPrice(itemPrice);
                esOps.save(eleBusiness);
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer addStoreItem(String id) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return -1;
        }
        Integer itemIndex = eleBusiness.getItemIndex() + 1;
        eleBusiness.setItemIndex(itemIndex);
        EleBusiness.StoreItem storeItem = new EleBusiness.StoreItem();
        storeItem.setItemId(itemIndex);
        List<EleBusiness.StoreItem> storeItems = eleBusiness.getStoreItems();
        if (storeItems == null) {
            return -1;
        }
        storeItems.add(storeItem);
        esOps.save(eleBusiness);
        return itemIndex;
    }

    @Override
    public boolean delStoreItem(String id, Integer itemId) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return false;
        }
        List<EleBusiness.StoreItem> storeItems = eleBusiness.getStoreItems();
        for (EleBusiness.StoreItem item : storeItems) {
            if (item.getItemId().equals(itemId)) {
                storeItems.remove(item);
                esOps.save(eleBusiness);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getStoreItemCover(String id, Integer itemId) {
        EleBusiness eleBusiness = esOps.get(id, EleBusiness.class);
        if (eleBusiness == null) {
            return null;
        }
        List<EleBusiness.StoreItem> storeItems = eleBusiness.getStoreItems();
        for (EleBusiness.StoreItem item : storeItems) {
            if (item.getItemId().equals(itemId)) {
                return item.getItemName();
            }
        }
        return null;
    }

    @Override
    public EleBusiness getEleBusinessById(String id) {
        return esOps.get(id, EleBusiness.class);
    }

    @Override
    public List<EleBusiness> getAllEleBusinessOrderById(String idToExclude) {
        Criteria criteria = new Criteria("_id").not().is(idToExclude);
        Query query = new CriteriaQuery(criteria)
                .addSort(Sort.by("_id").ascending());
        SearchHits<EleBusiness> searchHits = esOps.search(query, EleBusiness.class);
        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public List<EleBusiness> getALLEleBusinessByKeyword(String keyword, String idToExclude) {
        String queryJson = String.format("""
            {
              "bool": {
                "must": [
                  {
                    "bool": {
                      "should": [
                        {
                          "multi_match": {
                            "query": "%s",
                            "fields": ["store_name^3", "store_description"],
                            "type": "best_fields"
                          }
                        },
                        {
                          "nested": {
                            "path": "store_items",
                            "query": {
                              "multi_match": {
                                "query": "%s",
                                "fields": ["store_items.item_name^3", "store_items.item_description"],
                                "type": "best_fields"
                              }
                            }
                          }
                        }
                      ]
                    }
                  }
                ],
                "must_not": [
                  {
                    "term": {
                      "_id": "%s"
                    }
                  }
                ]
              }
            }
            """, keyword, keyword, idToExclude);

        StringQuery query = new StringQuery(queryJson);
        SearchHits<EleBusiness> searchHits = esOps.search(query, EleBusiness.class);
        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
