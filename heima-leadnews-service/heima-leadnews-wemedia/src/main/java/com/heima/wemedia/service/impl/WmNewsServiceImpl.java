package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.MQConstants;
import com.heima.common.dtos.AppHttpCodeEnum;
import com.heima.common.dtos.PageResponseResult;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.exception.LeadNewsException;
import com.heima.model.wemedia.dtos.WmNewsDownUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.*;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import com.heima.utils.common.ThreadLocalUtils;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmChannellService;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;
    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Override
    public PageResponseResult findList(WmNewsPageReqDto dto) {
        //参数检测
        dto.checkParam();

        //获取当前登录用户
        WmUser wmUser = (WmUser)ThreadLocalUtils.get();
        if(wmUser==null){
            throw new LeadNewsException(AppHttpCodeEnum.NEED_LOGIN);
        }

        IPage<WmNews> iPage = new Page<>(dto.getPage(),dto.getSize());

        QueryWrapper<WmNews> queryWrapper = new QueryWrapper<>();
        //判断用户
        queryWrapper.eq("user_id",wmUser.getId());

        //状态
        if(dto.getStatus()!=null){
            queryWrapper.eq("status",dto.getStatus());
        }

        //关键词
        if(StringUtils.isNotEmpty(dto.getKeyword())){
            queryWrapper.like("title",dto.getKeyword());
        }

        //频道
        if(dto.getChannelId()!=null){
            queryWrapper.eq("channel_id",dto.getChannelId());
        }

        //发布时间
        if(dto.getBeginPubDate()!=null && dto.getEndPubDate()!=null){
            queryWrapper.between("publish_time",dto.getBeginPubDate(),dto.getEndPubDate());
        }

        //排序
        queryWrapper.orderByDesc("created_time");

        iPage = page(iPage,queryWrapper);

        //封装分页数据
        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)iPage.getTotal());
        pageResponseResult.setData(iPage.getRecords());
        pageResponseResult.setCode(200);
        pageResponseResult.setErrorMessage("查询成功");
        return pageResponseResult;
    }

    @Override
    public ResponseResult submit(WmNewsDto dto) {
        //准备实体对象
        WmNews wmNews = BeanHelper.copyProperties(dto,WmNews.class);

        //获取当前登录用户
        WmUser wmUser = (WmUser)ThreadLocalUtils.get();
        if(wmUser==null){
            throw new LeadNewsException(AppHttpCodeEnum.NEED_LOGIN);
        }

        wmNews.setUserId(wmUser.getId());//文章作者ID

        //获取文章内容的图片
        List<String> contentImages = getContentImageFromNews(wmNews);

        if(dto.getType()==-1){
            //如果选自动封面
            int size = contentImages.size();
            if(size==0){
                //无图
                wmNews.setType((short)0);
                wmNews.setImages(null);
            }
            if(size>=1 && size<=2){
                //单图
                wmNews.setType((short)1);
                wmNews.setImages(contentImages.get(0));//取出第一张图片
            }
            if(size>=3){
                //三图（多图）
                wmNews.setType((short)3);
                /**
                 * subList(): 从集合截割出元素
                 */
                List<String> imageList = contentImages.subList(0,3);//包前不包后
                wmNews.setImages(imageList.stream().collect(Collectors.joining(",")));
        }
        }else{
            //没有选自动封面
            //type直接存入
            //把images使用逗号拼接成字符串存入
            List<String> images = dto.getImages();
            if(CollectionUtils.isNotEmpty(images)) {
                String imageStr = images.stream().collect(Collectors.joining(","));
                wmNews.setImages(imageStr);
            }
        }

        //判断操作为新增还是修改
        if(dto.getId()==null){
            wmNews.setCreatedTime(new Date());
            //新增
            save(wmNews);
        }else{
            //修改
            updateById(wmNews);

            //删除文章和旧素材的关系
            QueryWrapper<WmNewsMaterial> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("news_id",dto.getId());
            wmNewsMaterialMapper.delete(queryWrapper);
        }

        //绑定文章和素材关系

        //绑定内容素材与文章的关系
        //根据素材的URL地址查询素材ID
        List<Integer> materialIds = getMaterialIdsFromUrl(contentImages);
        if(CollectionUtils.isNotEmpty(materialIds)){
            wmNewsMaterialMapper.saveNewsMaterials(materialIds,wmNews.getId(),0);
        }

        //绑定封面素材与文章的关系
        String images = wmNews.getImages();
        if(StringUtils.isNotEmpty(images)){
            List<String> stringList = Arrays.asList(images.split(","));
            List<Integer> coverMaterialIds = getMaterialIdsFromUrl(stringList);
            if(CollectionUtils.isNotEmpty(coverMaterialIds)){
                wmNewsMaterialMapper.saveNewsMaterials(coverMaterialIds,wmNews.getId(),1);
            }
        }

        //提交审核
        if(wmNews.getStatus()==1){
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    /**
     * 根据素材的URL查询素材ID
     * @param images
     * @return
     */
    private List<Integer> getMaterialIdsFromUrl(List<String> images) {
        if(CollectionUtils.isNotEmpty(images)){
            QueryWrapper<WmMaterial> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("url",images);
            List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(queryWrapper);
            if(CollectionUtils.isNotEmpty(wmMaterials)){
                return wmMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());
            }
        }
        return null;
    }

    /**
     * 获取文章内容的所有图片
     * @param wmNews
     * @return
     */
    private List<String> getContentImageFromNews(WmNews wmNews) {
        List<String> contentImages = new ArrayList<>();
        if(StringUtils.isNotEmpty(wmNews.getContent())){
           List<Map> list = JsonUtils.toList(wmNews.getContent(), Map.class);
           for(Map map:list){
               if(map.get("type").equals("image")){
                   contentImages.add((String)map.get("value"));
               }
            }
        }
        return contentImages;
    }



    @Override
    public ResponseResult downOrUp(WmNewsDownUpDto dto) {

        //查询文章是否存在
        WmNews wmNews = getById(dto.getId());
        if(wmNews==null){
            throw new LeadNewsException(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        //判断文章是否已经发布
        if(wmNews.getStatus()!=9){
            throw new LeadNewsException(500,"文章必须先发布，再进行上下架操作");
        }

        //修改enable字段
        wmNews.setEnable(dto.getEnable());
        updateById(wmNews);

        Map<String,Object> msg = new HashMap<>();
        msg.put("articleId",wmNews.getArticleId());
        msg.put("enable",dto.getEnable());
        kafkaTemplate.send(MQConstants.WM_NEWS_UP_OR_DOWN_TOPIC,JsonUtils.toString(msg));

        //同步索引库
        if(dto.getEnable()==1){
            //上架
            kafkaTemplate.send(MQConstants.WM_NEW_UP_ES_TOPIC,wmNews.getArticleId().toString());
        }else{
            //下架
            kafkaTemplate.send(MQConstants.WM_NEW_DOWN_ES_TOPIC,wmNews.getArticleId().toString());
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


}
