package com.heima.behavior.service;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.behavior.dtos.LikesBehaviorDto;

public interface ApLikesBehaviorService {
    ResponseResult likesBehavior(LikesBehaviorDto dto);
}
