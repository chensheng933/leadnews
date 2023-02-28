package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.dtos.AppHttpCodeEnum;
import com.heima.common.dtos.PageResponseResult;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.exception.LeadNewsException;
import com.heima.common.minio.MinIOFileStorageService;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.BCrypt;
import com.heima.utils.common.JwtUtils;
import com.heima.utils.common.RsaUtils;
import com.heima.utils.common.ThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.service.WmUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {
    @Autowired
    private MinIOFileStorageService storageService;

    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        //校验参数
        if(multipartFile==null){
            throw new LeadNewsException(AppHttpCodeEnum.PARAM_INVALID);
        }

        //判断是否登录（获取登录用户信息）
        WmUser wmUser = (WmUser)ThreadLocalUtils.get();
        if(wmUser==null){
            throw new LeadNewsException(AppHttpCodeEnum.NEED_LOGIN);
        }

        try {
            //把图片存储到MinIO
            //生成随机文件名称
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            //获取文件后缀名
            String originalFilename = multipartFile.getOriginalFilename();
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".")); // .jpg
            String fileName = uuid+extName;
            String url = storageService.uploadImgFile("",fileName,multipartFile.getInputStream());

            //把素材信息存入DB
            WmMaterial wmMaterial = new WmMaterial();
            wmMaterial.setCreatedTime(new Date());
            wmMaterial.setUrl(url);
            wmMaterial.setUserId(wmUser.getId());
            wmMaterial.setType((short)0);
            wmMaterial.setIsCollection((short)0);
            save(wmMaterial);

            //返回素材信息
            return ResponseResult.okResult(wmMaterial);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageResponseResult findList(WmMaterialDto dto) {
        //参数处理
        dto.checkParam();

        //判断是否登录（获取登录用户信息）
        WmUser wmUser = (WmUser)ThreadLocalUtils.get();
        if(wmUser==null){
            throw new LeadNewsException(AppHttpCodeEnum.NEED_LOGIN);
        }

        //准备分页参数
        IPage<WmMaterial> iPage = new Page<>(dto.getPage(),dto.getSize());

        //准备条件参数
        QueryWrapper<WmMaterial> queryWrapper = new QueryWrapper<>();
        //判断当前登录用户
        queryWrapper.eq("user_id",wmUser.getId());

        //是否收藏
        if(dto.getIsCollection()!=null && dto.getIsCollection()==1){
            queryWrapper.eq("is_collection",dto.getIsCollection());
        }

        //排序
        queryWrapper.orderByDesc("created_time");

        //分页查询
        /**
         * iPage封装分页前后数据（page,size, 总页数，总记录数，List列表）
         */
        iPage = page(iPage,queryWrapper);

        //封装分页数据
        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)iPage.getTotal());
        pageResponseResult.setData(iPage.getRecords());
        pageResponseResult.setCode(200);
        pageResponseResult.setErrorMessage("查询成功");
        return pageResponseResult;
    }
}
