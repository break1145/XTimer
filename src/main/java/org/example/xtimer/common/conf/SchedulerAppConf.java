package org.example.xtimer.common.conf;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class SchedulerAppConf {

    @Value("${scheduler.bucketsNum}")
    private int bucketsNum;
    @Value("${scheduler.tryLockSeconds}")
    private int tryLockSeconds;
    @Value("${scheduler.tryLockGapMilliSeconds}")
    private int tryLockGapMilliSeconds;
    @Value("${scheduler.successExpireSeconds}")
    private int successExpireSeconds;

    @Value("${scheduler.pool.corePoolSize}")
    private int corePoolSize;

    @Value("${scheduler.pool.maxPoolSize}")
    private int maxPoolSize;

    @Value("${scheduler.pool.queueCapacity}")
    private int queueCapacity;

    @Value("${scheduler.pool.namePrefix}")
    private String namePrefix;


}