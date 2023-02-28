package com.heima;

import com.heima.utils.common.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class RsaTest {
    //公钥路径
    static final String publicKeyPath = "D:\\idea_codes\\javaee167\\rsa\\rsa-key.pub";
    //私钥路径
    static final String privateKeyPath = "D:\\idea_codes\\javaee167\\rsa\\rsa-key";

    /**
     * 生成一对公私钥文件
     */
    @Test
    public void testGenericKey() throws Exception {
        /**
         * 参数一：公钥路径
         * 参数二：私钥路径
         * 参数三：密文
         * 参数四：文件大小（字节）
         */
        RsaUtils.generateKey(publicKeyPath,privateKeyPath,"itheima1212",1024);
    }

    /**
     * 读取公钥数据
     */
    @Test
    public void testGetPublicKey() throws Exception {
        PublicKey publicKey = RsaUtils.getPublicKey(publicKeyPath);
        System.out.println(publicKey);
    }

    /**
     * 读取私钥数据
     */
    @Test
    public void testGetPrivateKey() throws Exception {
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        System.out.println(privateKey);
    }
}
