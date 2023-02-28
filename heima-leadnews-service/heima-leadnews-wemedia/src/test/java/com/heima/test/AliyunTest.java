package com.heima.test;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.minio.MinIOFileStorageService;
import com.heima.wemedia.WemediaApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WemediaApplication.class)
public class AliyunTest {
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private MinIOFileStorageService storageService;

    @Test
    public void testTextScan() throws Exception {
        List<String> textList = new ArrayList<>();
        textList.add("小明");
        textList.add("华为");
        textList.add("小米");
        textList.add("法论功");

        Map result = greenTextScan.greeTextScan(textList);

        System.out.println("最终的结果："+result.get("suggestion"));
    }

    @Test
    public void testImageScan() throws Exception {
        List<byte[]> imageList = new ArrayList<>();

        //从MinIO下载图片
        String url = "http://192.168.66.133:9000/leadnews/2022/06/26/8966083e9ffb4f36b2f40905bab161b9.jpeg";
        byte[] image = storageService.downLoadFile(url);
        imageList.add(image);

        Map result = greenImageScan.imageScan(imageList);

        System.out.println("最终的结果："+result.get("suggestion"));
    }
}
