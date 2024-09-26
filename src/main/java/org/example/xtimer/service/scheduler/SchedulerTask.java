package org.example.xtimer.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.example.xtimer.common.conf.SchedulerAppConf;
import org.example.xtimer.common.redis.ReentrantDistributeLock;
import org.example.xtimer.service.trigger.TriggerWorker;
import org.example.xtimer.utils.TimerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class SchedulerTask {

    @Autowired
    ReentrantDistributeLock reentrantDistributeLock;

    @Autowired
    SchedulerAppConf schedulerAppConf;

    @Autowired
    TriggerWorker triggerWorker;

    @Async("schedulerPool")
    public void asyncHandleSlice(Date date,int bucketId) {
        log.info("start executeAsync");
        String lockToken = TimerUtils.GetTokenStr();
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.GetTimeBucketLockKey(date,bucketId),
                lockToken,
                (long) schedulerAppConf.getTryLockSeconds());
        if(!ok){
            log.info("asyncHandleSlice 获取分布式锁失败");
            return;
        }
        log.info("get scheduler lock success, key: %s", TimerUtils.GetTimeBucketLockKey(date, bucketId));

        // 调用trigger进行处理
        // 分片编号：按照时间和桶编号二维划分
        triggerWorker.work(TimerUtils.GetSliceMsgKey(date,bucketId));

        // 延长分布式锁的时间,避免重复执行分片
        reentrantDistributeLock.expireLock(
                TimerUtils.GetTimeBucketLockKey(date,bucketId),
                lockToken,
                schedulerAppConf.getSuccessExpireSeconds());

        log.info("end executeAsync");

    }
}
