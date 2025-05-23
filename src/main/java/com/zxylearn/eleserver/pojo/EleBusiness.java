package com.zxylearn.eleserver.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.geo.Point;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "ele_business", createIndex = false)
public class EleBusiness {

    @Id
    private String id;

    @Field(name = "store_name")
    private String storeName;

    @Field(name = "store_description")
    private String storeDescription;

    @Field(name = "store_volume")
    private Integer storeVolume;

    @Field(name = "store_items")
    private List<StoreItem> storeItems;

    @Field(name = "location")
    private Point location;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreItem {
        @Field(name = "item_name")
        private String itemName;

        @Field(name = "item_description")
        private String itemDescription;

        @Field(name = "item_price")
        private float itemPrice;
    }
}
