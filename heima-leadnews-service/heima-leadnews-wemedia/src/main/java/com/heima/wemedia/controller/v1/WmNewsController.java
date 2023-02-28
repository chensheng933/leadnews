package com.heima.wemedia.controller.v1;

import com.heima.common.dtos.PageResponseResult;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDownUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {
    @Autowired
    private WmNewsService wmNewsService;

    /**
     * 自媒体文章查询
     */
    @PostMapping("/list")
    public PageResponseResult findList(@RequestBody WmNewsPageReqDto dto){
        return wmNewsService.findList(dto);
    }

    /**
     * 发表文章
     */
    @PostMapping("/submit")
    public ResponseResult submit(@RequestBody WmNewsDto dto){
        return wmNewsService.submit(dto);
    }

    /**
     * 文章查询（修改回显）
     */
    @GetMapping("/one/{id}")
    public ResponseResult one(@PathVariable("id") Integer id){
        return ResponseResult.okResult(wmNewsService.getById(id));
    }

    /**
     * 文章上下架
     */
    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDownUpDto dto){
        return wmNewsService.downOrUp(dto);
    }
}
