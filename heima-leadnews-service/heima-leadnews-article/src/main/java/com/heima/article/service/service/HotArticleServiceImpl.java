package com.heima.article.service.service;

import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.constants.RedisConstants;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ArticleVisitStreamMsg;
import com.heima.model.article.dtos.HotArticleVo;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import com.heima.wemedia.feign.WemediaFeign;
import io.seata.common.util.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotArticleServiceImpl implements HotArticleService {
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private WemediaFeign wemediaFeign;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void computeHotArticle() {
        //查询最近5天的文章
        //得到前第5天的时间
        Date lastDay = DateTime.now().minusDays(5).toDate();
        List<ApArticle> articleList = apArticleMapper.findArticleListByLastDays(lastDay);

        //计算文章的分值
        List<HotArticleVo> hotArticleVoList = computeHotArticleScore(articleList);

        //按照频道筛选文章，按照分值倒序，每个频道截取前30条数据，存入redis
        ResponseResult<List<WmChannel>> responseResult = wemediaFeign.channels();
        if(responseResult.getCode().equals(200)){
            List<WmChannel> channelList = responseResult.getData();

            if(CollectionUtils.isNotEmpty(channelList)){
                //缓存指定的频道
                for(WmChannel channel:channelList){

                   /* List<HotArticleVo> channelHotArticles = new ArrayList<>();

                    for(HotArticleVo vo:hotArticleVoList){
                        if(vo.getChannelId().equals(channel.getId())){
                            channelHotArticles.add(vo);
                        }
                    }*/

                    List<HotArticleVo> channelHotArticles = hotArticleVoList.stream()
                            .filter(vo->vo.getChannelId().equals(channel.getId()))
                            .collect(Collectors.toList());

                    //分值倒序，截取前30条，存入redis
                    String key = RedisConstants.HOT_ARTICLE_FIRST_PAGE+channel.getId();
                    sortAndCacheHotArticles(key,channelHotArticles);
                }

                //缓存推荐的频道
                String key = RedisConstants.HOT_ARTICLE_FIRST_PAGE+RedisConstants.DEFAULT_TAG;
                sortAndCacheHotArticles(key,hotArticleVoList);
            }
        }

    }


    /**
     * 对文章进行分值倒序，截取前30条，存入Redis
     * @param key
     * @param hotArticleVos
     */
    private void sortAndCacheHotArticles(String key, List<HotArticleVo> hotArticleVos) {
        //对文章按照分值进行倒序

        //方式一：使用Comparator对集合元素排序
        /**
         * 升序：o1.getScore()-o2.getScore()
         * 降序：o2.getScore()-o1.getScore()
         */
        /*hotArticleVos.sort(new Comparator<HotArticleVo>() {
            @Override
            public int compare(HotArticleVo o1, HotArticleVo o2) {
                return o2.getScore().compareTo(o1.getScore());
            }
        });*/

        //方式二：利用stream流对集合元素排序
        /**
         * 升序：默认使用sorted方法
         * 降序：sorted()+reversed()
         */
        hotArticleVos = hotArticleVos.stream()
                            .sorted(Comparator.comparing(HotArticleVo::getScore).reversed())
                            .collect(Collectors.toList());

        if(hotArticleVos.size()>30){
            //截取前30条
            hotArticleVos = hotArticleVos.subList(0,30);
        }

        //存入redis
        redisTemplate.opsForValue().set(key, JsonUtils.toString(hotArticleVos));
    }

    /**
     * 计算所有文章的分值
     * @param articleList
     * @return
     */
    private List<HotArticleVo> computeHotArticleScore(List<ApArticle> articleList) {
        List<HotArticleVo> hotArticleVoList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(articleList)){
            for(ApArticle apArticle:articleList){
                HotArticleVo hotArticleVo = BeanHelper.copyProperties(apArticle,HotArticleVo.class);
                Integer score = computeScore(apArticle);
                hotArticleVo.setScore(score);
                hotArticleVoList.add(hotArticleVo);
            }
        }
        return hotArticleVoList;
    }

    /**
     * 计算一篇文章的分值
     * @param apArticle
     * @return
     */
    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;

        //阅读数
        if(apArticle.getViews()!=null){
            score+=apArticle.getViews();
        }
        //点赞数
        if(apArticle.getLikes()!=null){
            score+=apArticle.getLikes()* ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        //评论数
        if(apArticle.getComment()!=null){
            score+=apArticle.getComment()* ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        //收藏数
        if(apArticle.getCollection()!=null){
            score+=apArticle.getCollection()* ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }

        return score;
    }


    @Override
    public void updateHotArticle(ArticleVisitStreamMsg streamMsg) {
        Long articleId = streamMsg.getArticleId();

        ApArticle apArticle = apArticleMapper.selectById(articleId);
        if(apArticle==null){
            return;
        }

        Long view = streamMsg.getView();
        Long like = streamMsg.getLike();
        Long comment = streamMsg.getComment();
        Long collect = streamMsg.getCollect();

        //更新DB表
        if(view!=null){
            apArticle.setViews(apArticle.getViews()+view.intValue());
        }
        if(like!=null){
            apArticle.setLikes(apArticle.getLikes()+like.intValue());
        }
        if(comment!=null){
            apArticle.setComment(apArticle.getComment()+comment.intValue());
        }
        if(collect!=null){
            apArticle.setCollection(apArticle.getCollection()+collect.intValue());
        }
        apArticleMapper.updateById(apArticle);

        //刷新首页文章缓存数据
        //指定频道缓存
        String key = RedisConstants.HOT_ARTICLE_FIRST_PAGE+apArticle.getChannelId();
        refreshHotArticleFromCache(key,apArticle);
        //推荐频道缓存
        key = RedisConstants.HOT_ARTICLE_FIRST_PAGE+RedisConstants.DEFAULT_TAG;
        refreshHotArticleFromCache(key,apArticle);
    }

    /**
     * 刷新redis缓存数据
     * @param key
     * @param apArticle
     */
    private void refreshHotArticleFromCache(String key, ApArticle apArticle) {
        //查询缓存数据
        String redisData = redisTemplate.opsForValue().get(key);
        List<HotArticleVo> hotArticleVoList = JsonUtils.toList(redisData, HotArticleVo.class);

        //计算当前更新文章的分值
        Integer score = computeScore(apArticle);
        HotArticleVo hotArticleVo = BeanHelper.copyProperties(apArticle,HotArticleVo.class);
        hotArticleVo.setScore(score);

        boolean flag = false;
        //当前更新文章已经在缓存中，直接更新缓存中的该文章的分值，重新分值倒序，重新入缓存
        for(HotArticleVo vo:hotArticleVoList){
            if(vo.getId().equals(hotArticleVo.getId())){
                //存在则更新分值
                vo.setScore(score);
                vo.setLikes(hotArticleVo.getLikes());
                vo.setCollection(hotArticleVo.getCollection());
                vo.setComment(hotArticleVo.getComment());
                vo.setViews(hotArticleVo.getViews());

                flag = true;
            }
        }

        //当前更新文章不在缓存中，将新文章存入缓存，重新分值倒序，截取前30条，重新入缓存
        if(!flag){
            hotArticleVoList.add(hotArticleVo);
        }

        //重新分值倒序，截取前30条，重新入缓存
        sortAndCacheHotArticles(key,hotArticleVoList);
    }
}
