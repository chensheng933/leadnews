package com.heima.search.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.heima.common.dtos.AppHttpCodeEnum;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.search.pojos.ApUserSearch;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.common.ThreadLocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApUserSearchServiceImpl implements ApUserSearchService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public void insert(Integer userId, String keyword) {

        //查询userId和keyword是否存在
        Query query = Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword));
        ApUserSearch userSearch = mongoTemplate.findOne(query, ApUserSearch.class);

        if(userSearch!=null){
            //该用户已经搜索过该关键词，只需要更新时间即可
            userSearch.setCreatedTime(new Date());
            mongoTemplate.save(userSearch);
            return;
        }

        //查询当前用户的记录
        Query userQuery = Query.query(Criteria.where("userId").is(userId));
        //对时间倒序
        userQuery.with(Sort.by(Sort.Direction.DESC,"createdTime"));
        List<ApUserSearch> searchList = mongoTemplate.find(userQuery, ApUserSearch.class);

        if(CollectionUtils.isEmpty(searchList) || searchList.size()<10){
            ApUserSearch apUserSearch = new ApUserSearch();
            apUserSearch.setUserId(userId);
            apUserSearch.setCreatedTime(new Date());
            apUserSearch.setKeyword(keyword);
            //不够10条记录，新增记录
            mongoTemplate.save(apUserSearch);
        }else{
            //够10条，更新最后1条记录
            ApUserSearch lastOne = searchList.get(searchList.size() - 1);
            lastOne.setKeyword(keyword);
            lastOne.setCreatedTime(new Date());
            mongoTemplate.save(lastOne);
        }
    }

    @Override
    public ResponseResult load() {
        ApUser apUser = (ApUser)ThreadLocalUtils.get();
        if(apUser==null){
            return ResponseResult.okResult(null);
        }

        Query query = Query.query(Criteria.where("userId").is(apUser.getId()));
        query.with(Sort.by(Sort.Direction.DESC,"createdTime"));

        List<ApUserSearch> searchList = mongoTemplate.find(query, ApUserSearch.class);

        return ResponseResult.okResult(searchList);
    }

    @Override
    public ResponseResult del(HistorySearchDto dto) {
        //根据id删除
        Query query = Query.query(Criteria.where("_id").is(dto.getId()));
        mongoTemplate.remove(query,ApUserSearch.class);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
