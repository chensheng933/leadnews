package com.heima.common.tess4j;

import lombok.Data;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tess4j")
public class Tess4JConfiguration {
    private String datapath;
    private String language;

    @Bean
    public ITesseract iTesseract(){
        ITesseract tesseract = new Tesseract();

        //设置语言包
        tesseract.setDatapath(datapath);//语言包的目录
        tesseract.setLanguage(language);//语言包的名称

        return tesseract;
    }
}
