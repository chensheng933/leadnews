package com.heima.search.service.impl;

import com.heima.article.feign.ApArticleFeign;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ArticleDoc;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.service.ApArticleSearchService;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import com.heima.utils.common.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class ApArticleSearchServiceImpl implements ApArticleSearchService {
    private static final String INDEX_NAME = "article";
    @Autowired
    private RestHighLevelClient highLevelClient;
    @Autowired
    private ApArticleFeign apArticleFeign;
    @Autowired
    private ApUserSearchService apUserSearchService;

    @Override
    public ResponseResult search(UserSearchDto dto) {
        try {
            if(dto.getPageSize()==0)dto.setPageSize(20);
            if(dto.getMinBehotTime()==null)dto.setMinBehotTime(new Date());

            //创建搜索对象
            SearchRequest request = new SearchRequest(INDEX_NAME);

            //添加条件
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.matchQuery("title",dto.getSearchWords()));
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("publishTime").lt(dto.getMinBehotTime().getTime()));
            request.source().query(boolQueryBuilder);

            request.source().size(dto.getPageSize());

            request.source().sort(SortBuilders.fieldSort("publishTime").order(SortOrder.DESC));

            request.source().highlighter(new HighlightBuilder().field("title")
                    .preTags("<font style='color: red; font-size: inherit;'>")
                    .postTags("</font>"));

            //执行查询
            SearchResponse response = highLevelClient.search(request, RequestOptions.DEFAULT);

            SearchHits hits = response.getHits();

            //遍历数据
            List<ArticleDoc> articleDocs = new ArrayList<>();
            for(SearchHit hit:hits){
                String json = hit.getSourceAsString();
                ArticleDoc articleDoc = JsonUtils.toBean(json, ArticleDoc.class);

                //获取高亮字段内容
                HighlightField highlightField = hit.getHighlightFields().get("title");
                if(highlightField!=null){
                    articleDoc.setH_title(highlightField.getFragments()[0].toString());
                }

                articleDocs.add(articleDoc);
            }

            //判断用户是否登录，只有登录才需要新增搜索记录
            ApUser apUser = (ApUser) ThreadLocalUtils.get();
            if(apUser!=null){
                apUserSearchService.insert(apUser.getId(),dto.getSearchWords());
            }

            return ResponseResult.okResult(articleDocs);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveToES(Long articeId) {
        try {
            //查询App文章
            ApArticle apArticle = apArticleFeign.findById(articeId);

            ArticleDoc articleDoc = BeanHelper.copyProperties(apArticle, ArticleDoc.class);

            IndexRequest request = new IndexRequest(INDEX_NAME).id(articleDoc.getId().toString());
            request.source(JsonUtils.toString(articleDoc), XContentType.JSON);

            highLevelClient.index(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("写入ES失败 {}",e.getMessage());
        }

    }

    @Override
    public void removeFromES(Long articleId) {
        try {
            DeleteRequest request = new DeleteRequest(INDEX_NAME).id(articleId.toString());
            highLevelClient.delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("移除ES数据失败 {}",e.getMessage());
        }
    }
}
