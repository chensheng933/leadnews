package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.common.dtos.PageResponseResult;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDownUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {

    PageResponseResult findList(WmNewsPageReqDto dto);

    ResponseResult submit(WmNewsDto dto);

    ResponseResult downOrUp(WmNewsDownUpDto dto);
}
