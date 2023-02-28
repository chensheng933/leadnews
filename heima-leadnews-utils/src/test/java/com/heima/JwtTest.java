package com.heima;

import com.heima.utils.common.JwtUtils;
import com.heima.utils.common.Payload;
import com.heima.utils.common.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

public class JwtTest {
    //公钥路径
    static final String publicKeyPath = "D:\\idea_codes\\javaee167\\rsa\\rsa-key.pub";
    //私钥路径
    static final String privateKeyPath = "D:\\idea_codes\\javaee167\\rsa\\rsa-key";

    /**
     * 生成token
     * @throws Exception
     */
    @Test
    public void testGenericToken() throws Exception {
        /**
         * 参数一：用户数据
         * 参数二：私钥数据
         * 参数三：过期时间（分）
         */
        User user = new User(1,"jack");

        //获取私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);

        String token = JwtUtils.generateTokenExpireInMinutes(user, privateKey, 1);

        System.out.println(token);
    }

    /**
     * 验证token
     */
    @Test
    public void testVerifyToken() throws Exception {
       String token = "eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoie1wiaWRcIjoxLFwibmFtZVwiOlwiamFja1wifSIsImp0aSI6Ik4yVXpaVFppWldZdFpEZ3dPUzAwTkRJMkxUaGhZV0V0T1RCa1lXTmpNRGd5WmprNCIsImV4cCI6MTY1NTY5OTAyNn0.A2EmhlOGwaunfByTgWjwNM_YyEwXPmaiiWO8W1vYO0a3SYrhWNBqQYUXTHBVdLRI5bdC9sUnkZ9GlvHNs8BA1nJQcr_V46iV79xOqbv3L-pHjkvKMoCE-_8lYi9hq5XCTpwlzS-V5rnRBHR7s8BCXK8AfVFeyY7lgbDgyhG0h0HBWK0fOt2nC6q-w8h3s1hOAHlHNDuY5ekcQ6eS0mEr00c-8wg3NMiasFSJviqX8ZAxyDc4_J7sFhZ5iyJtaGH_2gNlksXOV-uvTtBFVrUX86vQouXeJVQ5U1o0ZQmfaH2_PP9-pVVeXovhY1D93vf_BGNa7h09TCHkIKTm_aQcTg";

       //获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey(publicKeyPath);

        try {
            Payload<User> payload = JwtUtils.getInfoFromToken(token, publicKey, User.class);

            System.out.println("验证成功");

            Date expiration = payload.getExpiration();

            User user = payload.getInfo();

            System.out.println(user);

        } catch (Exception e) {
            System.out.println("验证失败");
            e.printStackTrace();

        }
    }
}
