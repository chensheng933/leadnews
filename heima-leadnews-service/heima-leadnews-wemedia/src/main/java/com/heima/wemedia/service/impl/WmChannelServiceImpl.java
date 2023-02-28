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
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.ThreadLocalUtils;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmChannellService;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Service
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannellService {

}
