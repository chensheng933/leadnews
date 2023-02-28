package com.heima.wemedia.service.impl;

import com.heima.common.constants.ScheduleConstants;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.schedule.feign.TaskFeign;
import com.heima.utils.common.JsonUtils;
import com.heima.wemedia.service.WmNewsTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WmNewsTaskServiceImpl implements WmNewsTaskService {
    @Autowired
    private TaskFeign taskFeign;

    @Override
    public Long addWmNewsTask(WmNews wmNews) {
        Task task = new Task();
        task.setTaskTopic(ScheduleConstants.TASK_TOPIC_NEWS_PUBLISH); //1 代表自媒体文章定时发布
        task.setExecuteTime(wmNews.getPublishTime().getTime());//必须自媒体定时发布时间
        WmNews news = new WmNews();
        news.setId(wmNews.getId());
        task.setParameters(JsonUtils.toString(news));
        Long taskId = taskFeign.addTask(task);
        return taskId;
    }
}
