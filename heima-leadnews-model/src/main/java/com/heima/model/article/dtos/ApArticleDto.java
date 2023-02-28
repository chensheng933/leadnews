package com.heima.model.article.dtos;

import com.heima.model.article.pojos.ApArticle;
import lombok.Data;

/**
 * 在自媒体端传递到App端的数据对象
 */
@Data
public class ApArticleDto extends ApArticle {
    private String content;
}
