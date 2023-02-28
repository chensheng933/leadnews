package com.heima.common;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Test;

import java.io.File;

public class Tessj4Test {

    @Test
    public void testScanImage() throws Exception {
        //创建Tesseract对象
        ITesseract tesseract = new Tesseract();

        //设置语言包
        tesseract.setDatapath("D:\\idea_codes\\javaee167\\ocr");//语言包的目录
        tesseract.setLanguage("chi_sim");//语言包的名称

        //扫描图片，获取文字
        String result = tesseract.doOCR(new File("d:/image-20210524161243572.png"));

        System.out.println(result);
    }
}
