package com.heima.search.controller.v1;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ApArticleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article/search")
public class ApArticleSearchController {
    @Autowired
    private ApArticleSearchService apArticleSearchService;


    @PostMapping("/search")
    public ResponseResult search(@RequestBody UserSearchDto dto){
        return apArticleSearchService.search(dto);
    }
}
