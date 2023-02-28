package com.heima.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StreamTest {

    @Test
    public void testList(){
        List<String> list = new ArrayList<>();
        list.add("http://192.168.200.130:9000/leadnews/2021/04/26/ef27ddb8b7a34193adb7f53f90d9fad0.jpg");
        list.add("http://192.168.200.130:9000/leadnews/2021/04/26/213b82900e544354b382cd7fa50b8421.jpg");
        list.add("http://192.168.200.130:9000/leadnews/2021/06/03/8d6292f456cc4c81a4ac4f71445606bf.jpg");

        String images = list.stream().collect(Collectors.joining(","));

        System.out.println(images);
    }
}
