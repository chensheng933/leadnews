package com.heima.model.article.pojos;

import lombok.Data;

import java.util.Date;

/**
 * 文章文档对象
 */
@Data
public class ArticleDoc {

    private Long id;

    private String title;

    private String h_title;//前端要求高亮显示的字段

    private Integer authorId;

    private String authorName;

    private Integer channelId;

    private String channelName;

    private Integer layout;

    private String images;

    private Integer likes;

    private Integer collection;

    private Integer comment;

    private Integer views;

    private Date createdTime;

    private Date publishTime;

    private String staticUrl;

}
