package com.heima.wemedia.feign;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("leadnews-wemedia")
public interface WemediaFeign {

    /**
     * 查询所有频道
     */
    @GetMapping("/api/v1/channel/channels")
    public ResponseResult<List<WmChannel>> channels();
}
