package com.heima.behavior.controller.v1;

import com.heima.behavior.service.ApLikesBehaviorService;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApLikesBehaviorController {
    @Autowired
    private ApLikesBehaviorService apLikesBehaviorService;

    /**
     * 点赞或取消点赞
     */
    @PostMapping("/api/v1/likes_behavior")
    public ResponseResult likesBehavior(@RequestBody LikesBehaviorDto dto){
        return apLikesBehaviorService.likesBehavior(dto);
    }
}
