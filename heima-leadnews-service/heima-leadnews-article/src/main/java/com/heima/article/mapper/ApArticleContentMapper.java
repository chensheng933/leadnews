package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApArticleContentMapper extends BaseMapper<ApArticleContent> {
}
