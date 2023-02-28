package com.heima.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.heima.common.constants.RedisConstants;
import com.heima.common.constants.ScheduleConstants;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public Long addTask(Task task) {

        //把任务添加到DB
        addTaskToDB(task);

        //把任务添加到Redis
        addTaskToCache(task);

        return task.getTaskId();
    }


    /**
     * 任务添加到redis
     * @param task
     */
    private void addTaskToCache(Task task) {
        //判断当前任务的执行时间是否在5分钟内，5分钟内的任务才存入Redis
        long futureDate = DateTime.now().plusMinutes(5).getMillis();
        if(task.getExecuteTime()<=futureDate){
            //存入redis的SortedSet
            String key = RedisConstants.TASK_TOPIC_PREFIX+task.getTaskTopic();
            redisTemplate.opsForZSet().add(key, JsonUtils.toString(task),task.getExecuteTime());
        }
    }

    /**
     * 把任务添加到DB
     * @param task
     */

    public void addTaskToDB(Task task) {
        try {
            //任务添加到任务表
            Taskinfo taskinfo = BeanHelper.copyProperties(task,Taskinfo.class);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            task.setTaskId(taskinfo.getTaskId());

            //任务添加到任务日志表
            TaskinfoLogs taskinfoLogs = BeanHelper.copyProperties(taskinfo,TaskinfoLogs.class);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);//初始化状态
            taskinfoLogs.setVersion(1);
            taskinfoLogsMapper.insert(taskinfoLogs);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public List<Task> pollTask(Integer taskTopic) {
        String key = RedisConstants.TASK_TOPIC_PREFIX+taskTopic;
        //查询redis中符合执行条件的任务
        Set<String> taskSet = redisTemplate.opsForZSet().rangeByScore(key, 0, System.currentTimeMillis());

        List<Task> taskList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(taskSet)){
            for(String taskJson:taskSet){
                Task task = JsonUtils.toBean(taskJson,Task.class);

                //更新DB数据
                updateTaskToDB(task);

                //删除redis数据
                redisTemplate.opsForZSet().remove(key,taskJson);

                taskList.add(task);
            }

        }
        return taskList;
    }

    /**
     * 更新DB的任务数据
     * @param task
     */
    private void updateTaskToDB(Task task) {
        try {
            //删除任务表记录
            taskinfoMapper.deleteById(task.getTaskId());

            //更新任务日志表记录
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(task.getTaskId());
            taskinfoLogs.setStatus(ScheduleConstants.EXECUTED);//已执行
            taskinfoLogsMapper.updateById(taskinfoLogs);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 定时同步DB数据到缓存
     *  fixedRate： 固定值（毫秒）
     */
    @Scheduled(fixedRate = 10000)
    public void syncDbToCache(){
        System.out.println("从DB同步任务到缓存"+new Date());

        //查询执行时间为5分钟内的任务，才需要导入缓存
        Date futureDate = DateTime.now().plusMinutes(5).toDate();
        QueryWrapper<Taskinfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("execute_time",futureDate);
        List<Taskinfo> taskinfoList = taskinfoMapper.selectList(queryWrapper);

        if(CollectionUtils.isNotEmpty(taskinfoList)){
            for(Taskinfo taskinfo:taskinfoList){
                Task task = BeanHelper.copyProperties(taskinfo,Task.class);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                String key = RedisConstants.TASK_TOPIC_PREFIX+task.getTaskTopic();
                redisTemplate.opsForZSet().add(key, JsonUtils.toString(task),task.getExecuteTime());
            }
        }
    }
}
