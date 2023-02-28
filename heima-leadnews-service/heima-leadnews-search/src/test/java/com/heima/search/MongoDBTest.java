package com.heima.search;

import com.heima.model.search.pojos.ApUserSearch;
import org.elasticsearch.search.SearchHit;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchApplication.class)
public class MongoDBTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 新增文档
     */
    @Test
    public void testSave(){

        //mongoTemplate.insert(null);//只有新增功能
        //mongoTemplate.save(null);//有新增和修改功能（有存在的ID主键则为修改）

        Random random = new Random();
        for(int i=1;i<=10;i++){
            ApUserSearch userSearch = new ApUserSearch();
            userSearch.setKeyword("今日头条"+i);
            userSearch.setUserId(2);
            userSearch.setCreatedTime(DateTime.now().minusMinutes(random.nextInt(10)).toDate());

            mongoTemplate.save(userSearch);
        }

    }

    /**
     * 查询文档
     */
    @Test
    public void testQuery(){
        //单条件查询
        //Query query = Query.query(Criteria.where("userId").is(1));

        //多条件查询   userId=1 且 keyword以"黑马"开头
        /**
         * regex(): 执行正则表达式搜索
         */
        //Query query = Query.query(Criteria.where("userId").is(1).and("keyword").regex("^黑马.*"));

        //分页查询
        /*Query query = Query.query(Criteria.where("userId").is(1));
        //query.limit(5);
        int page = 2;//页面传递的页码
        int size = 5;
        query.with(PageRequest.of(page-1,size));//page从0开始计算*/

        //排序
        Query query = Query.query(Criteria.where("userId").is(1));
        //时间倒序显示
        query.with(Sort.by(Sort.Direction.DESC,"createdTime"));

        List<ApUserSearch> userSearchList = mongoTemplate.find(query, ApUserSearch.class);
        userSearchList.forEach(System.out::println);
    }

    /**
     * 删除文档
     */
    @Test
    public void testDelete(){
        Query query = Query.query(Criteria.where("_id").is("62c10397d07632233083951a"));
        mongoTemplate.remove(query,ApUserSearch.class);
    }

}
