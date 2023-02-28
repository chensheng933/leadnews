package com.heima.schedule.feign;

import com.heima.model.schedule.dtos.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "leadnews-schedule",path = "/task")
public interface TaskFeign {
    /**
     * 添加延迟任务
     */
    @PostMapping("/addTask")
    public Long addTask(@RequestBody Task task);

    /**
     * 消费延迟任务
     */
    @PostMapping("/pollTask/{taskTopic}")
    public List<Task> pollTask(@PathVariable("taskTopic") Integer taskTopic);
}
