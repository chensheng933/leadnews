package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.user.pojos.ApUser;
import org.apache.ibatis.annotations.Param;
import org.simpleframework.xml.Path;

import java.util.Date;
import java.util.List;

public interface ApArticleMapper extends BaseMapper<ApArticle> {
    List<ApArticle> loadApArticle(@Param("dto") ArticleDto dto,@Param("type") int type);

    List<ApArticle> findArticleListByLastDays(@Path("lastDay") Date lastDay);
}
