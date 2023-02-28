package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

import java.util.List;

public interface TaskService {

    /**
     * 添加延迟任务
     * 返回任务ID
     */
    public Long addTask(Task task);

    /**
     * 消费延迟任务
     * 参数：需要消费的任务主题ID
     */
    public List<Task> pollTask(Integer taskTopic);
}
