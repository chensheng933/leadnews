package com.heima.schedule;

import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.heima.schedule.mapper")
@EnableDiscoveryClient  //服务注册发现
@EnableScheduling //开启定时器
public class ScheduleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScheduleApplication.class,args);
    }

    /**
     * MyBatisPlus乐观锁拦截器
     *  拦截器底层：
     *     没有加乐观锁：
     *           update taskinfo_logs set status = 1 where taskid = 1;
     *     加了乐观锁：
     *             select version from taskinfo_logs where taskid = 1
     *            update taskinfo_logs set status = 1 where taskid = 1 and version = ?;
     */
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor(){
        return new OptimisticLockerInterceptor();
    }
}
