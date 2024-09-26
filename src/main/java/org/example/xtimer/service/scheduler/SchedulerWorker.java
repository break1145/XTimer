package org.example.xtimer.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.example.xtimer.common.conf.SchedulerAppConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Component
@Slf4j
public class SchedulerWorker {

    @Autowired
    SchedulerTask schedulerTask;

    @Autowired
    SchedulerAppConf schedulerAppConf;

    @Scheduled(fixedRate = 1000)
    // 执行一维分片
    public void scheduledTask() {
        log.info("任务执行时间：" + LocalDateTime.now());
        handleSlices();
    }
    // 按照桶数量创建一维分片，分片中包含
    private void handleSlices(){
        for (int i = 0; i < schedulerAppConf.getBucketsNum(); i++) {
            handleSlice(i);
        }
    }

    private void handleSlice(int bucketId) {
        Date now = new Date();
        Date nowPreMin = new Date(now.getTime() - 60000);
        // 执行一分钟前的任务
        try {
            schedulerTask.asyncHandleSlice(nowPreMin,bucketId);
        }catch (Exception e){
            log.error("[handle slice] submit nowPreMin task failed, err:",e);
        }
        // 执行当前任务
        try {
            schedulerTask.asyncHandleSlice(now,bucketId);
        }catch (Exception e){
            log.error("[handle slice] submit now task failed, err:",e);
        }

    }

}
