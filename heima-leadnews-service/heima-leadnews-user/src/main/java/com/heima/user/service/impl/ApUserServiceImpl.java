package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.dtos.AppHttpCodeEnum;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.exception.LeadNewsException;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.BCrypt;
import com.heima.utils.common.JwtUtils;
import com.heima.utils.common.RsaUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {
    @Value("${leadnews.jwt.privateKeyPath}")
    private String privateKeyPath;
    @Value("${leadnews.jwt.expire}")
    private Integer expire;

    @Override
    public ResponseResult login(LoginDto dto) {
        if(StringUtils.isNotEmpty(dto.getPhone()) && StringUtils.isNotEmpty(dto.getPassword())){
            //手机号登录

            //验证账户是否存在
            QueryWrapper<ApUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone",dto.getPhone());// phone = xxx
            ApUser loginUser = getOne(queryWrapper);
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

        }else{
            //游客登录
            try {
                //读取私钥
                PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);

                //产生token
                ApUser user = new ApUser();
                user.setId(0);//0 代表游客
                String token = JwtUtils.generateTokenExpireInMinutes(user, privateKey, expire);

                Map<String,Object> resultMap = new HashMap<>();
                resultMap.put("token",token);

                //返回给前端
                return ResponseResult.okResult(resultMap);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

    }
}
