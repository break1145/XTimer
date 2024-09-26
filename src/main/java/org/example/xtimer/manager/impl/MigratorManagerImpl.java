package org.example.xtimer.manager.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.xtimer.common.conf.MigratorAppConf;
import org.example.xtimer.common.entity.Task;
import org.example.xtimer.common.entity.XTimer;
import org.example.xtimer.common.enums.TaskStatus;
import org.example.xtimer.common.enums.TimerStatus;
import org.example.xtimer.common.redis.ReentrantDistributeLock;
import org.example.xtimer.common.redis.TaskCache;
import org.example.xtimer.exception.BusinessException;
import org.example.xtimer.exception.ErrorCode;
import org.example.xtimer.manager.MigratorManager;
import org.example.xtimer.mapper.TaskMapper;
import org.example.xtimer.mapper.XTimerMapper;
import org.example.xtimer.utils.TimerUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MigratorManagerImpl implements MigratorManager {

    @Autowired
    private XTimerMapper timerMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ReentrantDistributeLock reentrantDistributeLock;

    @Autowired
    private MigratorAppConf migratorAppConf;

    @Autowired
    private TaskCache taskCache;

    /**
     * 激活Timer，迁移任务 将timer对应的任务迁移到mysql和redis数据库中
     * @param timer
     */
    @Override
    public void migrateTimer(XTimer timer) {
        // 校验状态
        if (timer.getStatus() != TimerStatus.Enable.getStatus()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Timer非Enable状态，迁移失败，timerId:"+timer.getTimerId());
        }

        //取得批量的执行时机
        CronExpression cronExpression;
        try {
            cronExpression = new CronExpression(timer.getCron());
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析cron表达式失败："+timer.getCron());
        }
        Date now = new Date();
        Date end = TimerUtils.GetForwardTwoMigrateStepEnd(now,migratorAppConf.getMigrateStepMinutes());

        List<Long> executeTimes = TimerUtils.GetCronNextsBetween(cronExpression,now,end);
        if (CollectionUtils.isEmpty(executeTimes) ){
            log.warn("获取执行时机 executeTimes 为空");
            return;
        }
        // 获取执行时机，加入数据库
        List<Task> taskList = batchTasksFromTimer(timer,executeTimes);
        // 加入mysql
        taskMapper.saveBatch(taskList);
        // 加入redis ZSet
        boolean cacheRes = taskCache.cacheSaveTasks(taskList);
        if (!cacheRes) {
            log.error("Zset存储taskList失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ZSet存储taskList失败，timerId:"+timer.getTimerId());
        }
    }

    /**
     * 根据timerId和执行时机生成对应任务
     * @return 包含一系列任务的表
     */
    public List<Task> batchTasksFromTimer(XTimer timer, List<Long> executeTimes){
        if (CollectionUtils.isEmpty(executeTimes) || timer == null) {
            return null;
        }
        List<Task> taskList = new ArrayList<>();
        for (Long executeTime : executeTimes) {
            Task task = new Task();
            task.setApp(timer.getApp());
            task.setTimerId(timer.getTimerId());
            task.setRunTimer(executeTime);
            task.setStatus(TaskStatus.NotRun.getStatus());
            taskList.add(task);
        }
        return taskList;
    }


    }
