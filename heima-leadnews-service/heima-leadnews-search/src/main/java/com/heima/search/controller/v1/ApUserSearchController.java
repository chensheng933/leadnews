package com.heima.search.controller.v1;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.search.service.ApUserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history")
public class ApUserSearchController {
    @Autowired
    private ApUserSearchService apUserSearchService;

    /**
     * 查询历史记录
     */
    @PostMapping("/load")
    public ResponseResult load(){
        return apUserSearchService.load();
    }

    /**
     * 删除历史记录
     */
    @PostMapping("/del")
    public ResponseResult del(@RequestBody HistorySearchDto dto){
        return apUserSearchService.del(dto);
    }
}
