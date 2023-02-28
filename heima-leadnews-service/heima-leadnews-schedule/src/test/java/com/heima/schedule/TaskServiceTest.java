package com.heima.schedule;

import com.baomidou.mybatisplus.extension.api.R;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleApplication.class)
public class TaskServiceTest {
    @Autowired
    private TaskService taskService;

    /**
     * 添加延迟任务
     */
    @Test
    public void testAddTask(){
        Random random = new Random();
        for(int i=1;i<=20;i++){
            Task task = new Task();
            task.setTaskTopic(1);
            task.setExecuteTime(DateTime.now().plusMinutes(random.nextInt(10)).getMillis());
            task.setParameters("task"+i);
            taskService.addTask(task);
        }
    }

    /**
     * 消费延迟任务
     */
    @Test
    public void testPollTask(){
        List<Task> taskList =  taskService.pollTask(1);

    }
}
