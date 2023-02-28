package com.heima.search.service;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

public interface ApArticleSearchService {
    ResponseResult search(UserSearchDto dto);

    void saveToES(Long valueOf);

    void removeFromES(Long valueOf);
}
