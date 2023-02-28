package com.heima.search;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.heima.article.feign.ApArticleFeign;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ArticleDoc;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchApplication.class)
public class ArticleTest {
    @Autowired
    private RestHighLevelClient highLevelClient;

    @Autowired
    private ApArticleFeign apArticleFeign;

    /**
     * 导入数据到ES
     */
    @Test
    public void testImportData() throws Exception {
        //查询数据
        List<ApArticle> articleList = apArticleFeign.findApArticles();

        if(CollectionUtils.isNotEmpty(articleList)){
            //创建批量操作对象
            BulkRequest bulkRequest = new BulkRequest();

            for(ApArticle apArticle:articleList){
                //拷贝数据到ArticleDoc
                ArticleDoc articleDoc = BeanHelper.copyProperties(apArticle, ArticleDoc.class);

                IndexRequest request = new IndexRequest("article").id(articleDoc.getId().toString());
                request.source(JsonUtils.toString(articleDoc), XContentType.JSON);

                //添加到缓存对象
                bulkRequest.add(request);
            }

            //执行批量写入
            highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        }

    }
}
