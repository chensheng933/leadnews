package com.heima.schedule.controller;

import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private TaskService taskService;
    /**
     * 添加延迟任务
     */
    @PostMapping("/addTask")
    public Long addTask(@RequestBody Task task){
        return taskService.addTask(task);
    }

    /**
     * 消费延迟任务
     */
    @PostMapping("/pollTask/{taskTopic}")
    public List<Task> pollTask(@PathVariable("taskTopic") Integer taskTopic){
        return taskService.pollTask(taskTopic);
    }
}
