package com.heima.article.feign;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.article.pojos.ApArticle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * App文章Feign接口
 */
@FeignClient(name = "leadnews-article",path = "/api/v1/article")
public interface ApArticleFeign {

    /**
     * 保存App文章
     */
    @PostMapping("/save")
    public ResponseResult<Long> save(@RequestBody ApArticleDto dto);

    /**
     * 查询所有文章数据
     */
    @GetMapping("/findApArticles")
    public List<ApArticle> findApArticles();

    /**
     * 根据id查询文章
     */
    @GetMapping("/findById/{id}")
    public ApArticle findById(@PathVariable("id") Long id);
}
