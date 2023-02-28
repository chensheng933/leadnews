package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/article")
public class ApArticleController{
    @Autowired
    private ApArticleService apArticleService;
    /**
     * 首页文章
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleDto dto){
       //return apArticleService.loadApArticle(dto,1); //1 代表上拉(小于判断)
        return apArticleService.loadApArticle2(dto,1); //1 代表上拉(小于判断)
    }

    /**
     * 更多文章
     */
    @PostMapping("/loadmore")
    public ResponseResult loadmore(@RequestBody ArticleDto dto){
        return apArticleService.loadApArticle(dto,1); //1 代表上拉(小于判断)
    }

    /**
     * 更新文章
     */
    @PostMapping("/loadnew")
    public ResponseResult loadnew(@RequestBody ArticleDto dto){
        return apArticleService.loadApArticle(dto,2); //2 代表下拉（大于判断）
    }

    /**
     * 保存App文章
     */
    @PostMapping("/save")
    public ResponseResult save(@RequestBody ApArticleDto dto){
        return ResponseResult.okResult(apArticleService.saveApArticle(dto));
    }

    /**
     * 查询所有文章数据
     */
    @GetMapping("/findApArticles")
    public List<ApArticle> findApArticles(){
        return apArticleService.list();
    }

    /**
     * 根据id查询文章
     */
    @GetMapping("/findById/{id}")
    public ApArticle findById(@PathVariable("id") Long id){
        return apArticleService.getById(id);
    }
}
