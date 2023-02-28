package com.heima.model.search.pojos;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 *  MongoDB        MySQL    Elasticsearch
 *  Database       Database      ---
 *  Collection     Table        Index
 * Document(bson)   Row         Document(json)
 * Field           Column       Field
 *  _id            id           _id
 */
@Data
@Document(collection = "ap_user_search")  //collection集合名，类似于表名
public class ApUserSearch {
    @Id  // 映射_id
    private String id; //主键

    //@Field(name = "user_id")
    private Integer userId; //该记录的用户ID

    private String keyword;//搜索词

    private Date createdTime;//记录创建时间
}
