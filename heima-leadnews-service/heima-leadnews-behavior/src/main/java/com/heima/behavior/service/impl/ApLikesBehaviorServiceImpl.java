package com.heima.behavior.service.impl;

import com.heima.behavior.service.ApLikesBehaviorService;
import com.heima.common.constants.MQConstants;
import com.heima.common.constants.RedisConstants;
import com.heima.common.dtos.AppHttpCodeEnum;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.exception.LeadNewsException;
import com.heima.model.article.dtos.UpdateArticleMsg;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.JsonUtils;
import com.heima.utils.common.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ApLikesBehaviorServiceImpl implements ApLikesBehaviorService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Override
    public ResponseResult likesBehavior(LikesBehaviorDto dto) {
        //判断是否登录
        ApUser apUser = (ApUser)ThreadLocalUtils.get();
        if(apUser==null){
            throw new LeadNewsException(AppHttpCodeEnum.NEED_LOGIN);
        }

        String key = RedisConstants.LIKE_BEHAVIOR+dto.getArticleId().toString();

        if(dto.getOperation()==0){
            //点赞
            Map value = new HashMap<>();
            value.put("createdTime",new Date());
            redisTemplate.opsForHash().put(key,apUser.getId().toString(), JsonUtils.toString(value));

            UpdateArticleMsg articleMsg = new UpdateArticleMsg();
            articleMsg.setArticleId(dto.getArticleId());
            articleMsg.setType(UpdateArticleMsg.UpdateArticleType.LIKES);

            kafkaTemplate.send(MQConstants.HOT_ARTICLE_INPUT_TOPIC,JsonUtils.toString(articleMsg));

            log.info("点赞后，发送消息完成");
        }else{
            //取消点赞
            redisTemplate.opsForHash().delete(key,apUser.getId().toString());
        }

        //redisTemplate.opsForHash().hasKey(key,key);//查询当前用户是否对文章点赞过
        //redisTemplate.opsForHash().get(key,key);//获取当前文章的当前用户的点赞数据

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
