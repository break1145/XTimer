package org.example.xtimer.service.trigger;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.xtimer.common.conf.TriggerAppConf;
import org.example.xtimer.common.entity.Task;
import org.example.xtimer.common.redis.TaskCache;
import org.example.xtimer.mapper.TaskMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class TriggerTimerTask extends TimerTask {

    TriggerAppConf triggerAppConf;

    TriggerPoolTask triggerPoolTask;

    TaskCache taskCache;

    TaskMapper taskMapper;

    private CountDownLatch latch;

    private Date startTime;

    private Date endTime;

    private String minuteBucketKey;

    private Long count = 0L;

    public TriggerTimerTask(TriggerAppConf triggerAppConf, TriggerPoolTask triggerPoolTask,
                            TaskCache taskCache, TaskMapper taskMapper, CountDownLatch latch,
                            Date startTime, Date endTime, String minuteBucketKey) {
        this.triggerAppConf = triggerAppConf;
        this.triggerPoolTask = triggerPoolTask;
        this.taskCache = taskCache;
        this.taskMapper = taskMapper;
        this.latch = latch;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minuteBucketKey = minuteBucketKey;
    }

    @Override
    public void run() {
        // 计算出从起始时间到当前批次的时间偏移量
        Date tStart = new Date(startTime.getTime() + count * triggerAppConf.getZrangeGapSeconds() * 1000L);
        if (tStart.compareTo(endTime) > 0) {
            latch.countDown();
            return;
        }
        // 处理单个批次
        try {
            handleBatch(tStart, new Date(tStart.getTime() + triggerAppConf.getZrangeGapSeconds() * 1000L));
        } catch (Exception e) {
            log.error("handleBatch Error. minuteBucketKey: " + minuteBucketKey + ", tStartTime: " + startTime + " ,e: ", e);
        }
        count++;
    }

    private void handleBatch(Date start, Date end) {
        List<Task> tasks = getTasksByTime(start, end);
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        for (Task task : tasks) {
            try {
                if (task == null) {
                    continue;
                }
                triggerPoolTask.runExecutor(task);
            } catch (Exception e) {
                log.error("executor run task error,task" + task);
            }
        }
    }


    private List<Task> getTasksByTime(Date start, Date end) {
        List<Task> tasks = new ArrayList<>();
        try {
            tasks = taskCache.getTasksFromCache(minuteBucketKey, start.getTime(), end.getTime());
        } catch (Exception e) {
            log.error("从缓存获取任务列表失败："+ e.getMessage());
            try {
                tasks = taskMapper.getTaskByTimeRange(start.getTime(), end.getTime());
            } catch (Exception e1) {
                log.error("从数据库获取任务列表失败" + e1.getMessage());
            }
        }
        return tasks;
    }
}