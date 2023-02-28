package com.heima.schedule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleApplication.class)
public class RedisTest {
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 往SortedSet添加元素
     */
    @Test
    public void testSet(){
        //默认按照score值升序存储
        for(int i=1;i<=20;i++){
            redisTemplate.opsForZSet().add("hello","jack"+i,i);
        }

    }

    /**
     * 从SortedSet查询元素
     */
    @Test
    public void testGet(){
        //按照元素下标范围查询
        /*Set<String> set = redisTemplate.opsForZSet().range("hello", 3, 8);
        System.out.println(set);*/

        //按照元素的score范围查询
       // Set<String> set = redisTemplate.opsForZSet().rangeByScore("hello", 10, 15);

        //查询score值小于等于13的元素
        Set<String> set = redisTemplate.opsForZSet().rangeByScore("hello", 0, 13);

        System.out.println(set);
    }

    /**
     * 删除SortedSet元素
     */
    @Test
    public void testRemove(){
        //删除指定元素
        //redisTemplate.opsForZSet().remove("hello","jack12");

        //根据score范围移除
        redisTemplate.opsForZSet().removeRangeByScore("hello",10,15);
    }
}

