package com.heima.model.article.dtos;

import lombok.Data;

@Data
public class ArticleVisitStreamMsg {
    /**
     * 文章id
     */
    private Long articleId;
    /**
     * 阅读
     */
    private Long view;
    /**
     * 收藏
     */
    private Long collect;
    /**
     * 评论
     */
    private Long comment;
    /**
     * 点赞
     */
    private Long like;
}
