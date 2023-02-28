package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.dtos.AppHttpCodeEnum;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.exception.LeadNewsException;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.BCrypt;
import com.heima.utils.common.JwtUtils;
import com.heima.utils.common.RsaUtils;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

@Service
public class WmUserServiceImpl extends ServiceImpl<WmUserMapper, WmUser> implements WmUserService {
    @Value("${leadnews.jwt.privateKeyPath}")
    private String privateKeyPath;
    @Value("${leadnews.jwt.expire}")
    private Integer expire;

    @Override
    public ResponseResult login(WmLoginDto dto) {
        //处理参数
        if(StringUtils.isEmpty(dto.getName()) || StringUtils.isEmpty(dto.getPassword())){
            throw new LeadNewsException(AppHttpCodeEnum.PARAM_INVALID);
        }

        //验证账户是否存在
        QueryWrapper<WmUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",dto.getName());
        WmUser loginUser = getOne(queryWrapper);
        if(loginUser==null){
            //throw new RuntimeException("账户不存在");
            //throw new LeadNewsException(404,"账户不存在");
            throw new LeadNewsException(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }
        //验证密码是否正确
        if(!BCrypt.checkpw(dto.getPassword(),loginUser.getPassword())){
            //throw new RuntimeException("密码错误");
            //throw new LeadNewsException(500,"密码错误");
            throw new LeadNewsException(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }

        try {
            //读取私钥
            PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);

            //产生token
            loginUser.setPassword(null);//去掉敏感信息
            String token = JwtUtils.generateTokenExpireInMinutes(loginUser, privateKey, expire);

            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("token",token);
            resultMap.put("user",loginUser);

            //返回给前端
            return ResponseResult.okResult(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
