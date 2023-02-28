package com.heima;

import com.heima.utils.common.BCrypt;
import org.junit.Test;

public class BCrptTest {

    @Test
    public void testEncode(){
        String password = "admin";

        //生成随机盐
        String salt = BCrypt.gensalt();
        System.out.println(salt);

        //加密
        String hashpw = BCrypt.hashpw(password, salt);

        System.out.println(hashpw);
    }

    @Test
    public void testCheckPwd(){
        String password = "123456";
        String hashpwd = "$2a$10$VEkwToeHgj1U9dH4NARDbuJ4ly2X85KF2udPa6PfYRx7rEgc4FeZy";

        boolean flag = BCrypt.checkpw(password, hashpwd);
        System.out.println(flag);
    }
}
