package com.heima.wemedia.schedule;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.heima.common.constants.ScheduleConstants;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.schedule.feign.TaskFeign;
import com.heima.utils.common.JsonUtils;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WmNewsTaskJob {
    @Autowired
    private TaskFeign taskFeign;
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Scheduled(fixedRate = 1000)
    public void pollWmNewsTask(){
        //到延迟队列中拉取任务
        List<Task> taskList = taskFeign.pollTask(ScheduleConstants.TASK_TOPIC_NEWS_PUBLISH);
        if(CollectionUtils.isNotEmpty(taskList)){
            for(Task task:taskList){
                //从Task取出文章ID
                WmNews wmNews = JsonUtils.toBean(task.getParameters(),WmNews.class);

                //查询文章
                wmNews = wmNewsMapper.selectById(wmNews.getId());

                //发布文章
                wmNewsAutoScanService.publishApArticle(wmNews);

                System.out.println("文章已经定时发布成功");
            }
        }
    }
}
