package com.heima.article.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.heima.common.exception")   // 扫描Spring组件
public class ExceptionConfig {
}
