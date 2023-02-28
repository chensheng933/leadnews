package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.heima.article.feign.ApArticleFeign;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.constants.MQConstants;
import com.heima.common.constants.RedisConstants;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.minio.MinIOFileStorageService;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsTaskService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private MinIOFileStorageService storageService;
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private ApArticleFeign apArticleFeign;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmUserMapper wmUserMapper;
    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ITesseract tesseract;
    @Autowired
    private WmNewsTaskService wmNewsTaskService;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Override
    @Async  //该方法会放在一个独立的线程中被执行
    @GlobalTransactional  //加入全局事务
    public void autoScanWmNews(Integer id) {
        //根据id查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);

        //判断是否为“提交审核”状态
        if(wmNews.getStatus()!=1){
            return;
        }

        //从文章提取图片（从MinIO下载图片）
        List<byte[]> imageList = getImagesFromNews(wmNews);

        //从文章中提取文本
        List<String> textList = getTextFromNews(wmNews,imageList);


        //检测自定义敏感词
        if(CollectionUtils.isNotEmpty(textList)){
            boolean flag = handleSensitiveScan(textList,wmNews);
            if(!flag){
                return;//如果审核失败则退出
            }
        }


        //把文本提交阿里云检测，根据结果文章修改
        if(CollectionUtils.isNotEmpty(textList)){
            try {
                Map result = greenTextScan.greeTextScan(textList);
                boolean flag = handleScanResult(result,wmNews);
                if(!flag){
                    return;//如果审核失败则退出审核
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("调用阿里云API失败，{}",e.getMessage());
            }
        }

        //把图片提交阿里云检测，根据结果文章修改
        if(CollectionUtils.isNotEmpty(imageList)){
            try {
                Map result = greenImageScan.imageScan(imageList);
                boolean flag = handleScanResult(result,wmNews);
                if(!flag){
                    return;//如果审核失败则退出审核
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("调用阿里云API失败，{}",e.getMessage());
            }
        }

        //判断发布时间大于当前时间，修改文章状态为8（待发布）
        if(wmNews.getPublishTime()!=null && wmNews.getPublishTime().after(new Date())){
            wmNews.setStatus(WmNews.Status.SUCCESS.getCode());
            wmNews.setReason("文章审核成功，待发布");
            wmNewsMapper.updateById(wmNews);

            //把当前自媒体文章发布任务添加到延迟队列中
            Long taskId = wmNewsTaskService.addWmNewsTask(wmNews);
            //更新wm_news表的task_id字段
            log.info("文章已进入延迟队列，待发布...");
            return; // 必须退出
        }

        //发布文章（文章保存App端 Feign接口）
        publishApArticle(wmNews);
    }

    /**
     * 自定义敏感词检测
     * @param textList
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(List<String> textList, WmNews wmNews) {
        boolean flag = true;

        //从redis查询数据
        List<String> wordList = null;
        String redisData = redisTemplate.opsForValue().get(RedisConstants.SENSITIVE_WORD);
        if(StringUtils.isEmpty(redisData)){
            //从数据库查询所有敏感词
            List<WmSensitive> sensitiveList = wmSensitiveMapper.selectList(null);
            if(CollectionUtils.isNotEmpty(sensitiveList)){
                wordList = sensitiveList.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

                //把敏感词存入redis
                redisTemplate.opsForValue().set(RedisConstants.SENSITIVE_WORD,JsonUtils.toString(wordList));
            }
        }else{
            //转换格式
            wordList = JsonUtils.toList(redisData,String.class);
        }

        //构建敏感词词库
        SensitiveWordUtil.initMap(wordList);

        if(CollectionUtils.isNotEmpty(textList)){
            String content = textList.stream().collect(Collectors.joining(""));
            //匹配敏感词库
            Map<String, Integer> result = SensitiveWordUtil.matchWords(content);

            if(result!=null && result.size()>0){
                //获取违规词
                Set<String> keys = result.keySet();
                //修改文章状态
                wmNews.setStatus(WmNews.Status.FAIL.getCode());
                wmNews.setReason("文章包含违规词："+keys);
                wmNewsMapper.updateById(wmNews);

                flag = false;
            }
        }

        return flag;
    }

    /**
     * 发布App文章
     * @param wmNews
     */
    public void publishApArticle(WmNews wmNews) {
        ApArticleDto dto = BeanHelper.copyProperties(wmNews,ApArticleDto.class);

        //设置类型
        dto.setLayout(wmNews.getType());
        //文章标记
        dto.setFlag((byte)0);

        //频道
        WmChannel channel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(channel!=null){
            dto.setChannelId(channel.getId());
            dto.setChannelName(channel.getName());
        }

        //文章作者
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if(wmUser!=null){
            dto.setAuthorId(Long.valueOf(wmUser.getId()));
            dto.setAuthorName(wmUser.getNickname());
        }

        //文章的行为数据
        dto.setLikes(0);
        dto.setComment(0);
        dto.setCollection(0);
        dto.setViews(0);

        //这行代码用于标记当前保存App文章是否为新增还是修改
        dto.setId(wmNews.getArticleId());

        ResponseResult<Long> responseResult = apArticleFeign.save(dto);
        if(responseResult.getCode().equals(200)){
            Long articleId = responseResult.getData();

            //模拟异常
            //int i = 100/0;

            wmNews.setArticleId(articleId);
            wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());
            wmNews.setReason("文章已发布");
            wmNewsMapper.updateById(wmNews);
        }

        //文章同步ES索引库
        kafkaTemplate.send(MQConstants.WM_NEW_UP_ES_TOPIC,wmNews.getArticleId().toString());
    }

    private boolean handleScanResult(Map result, WmNews wmNews) {
        boolean flag = false;
        String suggestion = (String)result.get("suggestion");
        if(StringUtils.isNotEmpty(suggestion)){
            if("pass".equals(suggestion)){
                flag = true;//审核成功
            }
            if("block".equals(suggestion)){
                //修改文章状态
                wmNews.setStatus(WmNews.Status.FAIL.getCode());
                wmNews.setReason("文章包含违规内容");
                wmNewsMapper.updateById(wmNews);
            }
            if("review".equals(suggestion)){
                wmNews.setStatus(WmNews.Status.ADMIN_AUTH.getCode());
                wmNews.setReason("文章待进一步人工审核");
                wmNewsMapper.updateById(wmNews);
            }
        }

        return flag;
    }

    /**
     * 从文章中提取图片
     * @param wmNews
     * @return
     */
    private List<byte[]> getImagesFromNews(WmNews wmNews) {
        Set<String> imageUrls = new HashSet<>(); //为了去除重复的url
        //封面
        if(StringUtils.isNotEmpty(wmNews.getImages())){
            String[] array = wmNews.getImages().split(",");
            for(String url:array){
                imageUrls.add(url);
            }
        }
        //内容的图片部分
        if(StringUtils.isNotEmpty(wmNews.getContent())){
            List<Map> list = JsonUtils.toList(wmNews.getContent(), Map.class);
            for(Map map:list){
                if(map.get("type").equals("image")){
                    imageUrls.add((String)map.get("value"));
                }
            }
        }

        //从MinIO下载所有图片
        List<byte[]> imageList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(imageUrls)){
            for(String url:imageUrls){
                byte[] image = storageService.downLoadFile(url);
                imageList.add(image);
            }
        }
        return imageList;
    }

    /**
     * 从文章中提取文本
     * @param wmNews
     * @return
     */
    private List<String> getTextFromNews(WmNews wmNews,List<byte[]> imageList) {
        List<String> textList = new ArrayList<>();

        //标题
        if(StringUtils.isNotEmpty(wmNews.getTitle())){
            textList.add(wmNews.getTitle());
        }

        //标签
        if(StringUtils.isNotEmpty(wmNews.getLabels())){
            textList.add(wmNews.getLabels());
        }

        //内容的文本部分
        if(StringUtils.isNotEmpty(wmNews.getContent())){
            List<Map> list = JsonUtils.toList(wmNews.getContent(), Map.class);
            for(Map map:list){
                if(map.get("type").equals("text")){
                    textList.add((String)map.get("value"));
                }
            }
        }

        //扫描文章的所有图片，识别出文字，存入textList
        if(CollectionUtils.isNotEmpty(imageList)){
            for(byte[] image:imageList){
                try {
                    InputStream inputStream = new ByteArrayInputStream(image);
                    BufferedImage bufferedImage = ImageIO.read(inputStream);
                    String result = tesseract.doOCR(bufferedImage);
                    if(StringUtils.isNotEmpty(result)){
                        textList.add(result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("识别出错，{}",e.getMessage());
                }
            }
        }

        return textList;
    }
}
