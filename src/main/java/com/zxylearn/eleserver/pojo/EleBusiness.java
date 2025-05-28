package com.zxylearn.eleserver.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
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

    @Field(name = "store_cover")
    private String storeCover;

    @Field(name = "store_items", type = FieldType.Nested)
    private List<StoreItem> storeItems;

    @Field(name = "location")
    private Point location;

    @Field(name = "location_text")
    private String locationText;

    @Field(name = "store_volume")
    private Integer storeVolume;

    @Field(name = "item_index")
    private Integer itemIndex;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreItem {
        @Field(name = "item_id")
        private Integer itemId;

        @Field(name = "item_name")
        private String itemName;

        @Field(name = "item_description")
        private String itemDescription;

        @Field(name = "item_cover")
        private String itemCover;

        @Field(name = "item_price")
        private Float itemPrice;
    }
}
