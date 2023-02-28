package com.heima.wemedia;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.heima.wemedia.mapper")
@EnableDiscoveryClient  //服务注册发现
@EnableFeignClients("com.heima.*.feign") //扫描Feign即可
@EnableAsync //开启异步线程注解
@EnableScheduling //开启定时器
public class WemediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemediaApplication.class,args);
    }

    /**
     * MyBatisPlus分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }
}
