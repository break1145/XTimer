package org.example.xtimer.common.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class TriggerAppConf {
    @Value("${trigger.zrangeGapSeconds}")
    private int zrangeGapSeconds;
    @Value("${trigger.workersNum}")
    private int workersNum;

    @Value("${trigger.pool.corePoolSize}")
    private int corePoolSize;

    @Value("${trigger.pool.maxPoolSize}")
    private int maxPoolSize;

    @Value("${trigger.pool.queueCapacity}")
    private int queueCapacity;

    @Value("${trigger.pool.namePrefix}")
    private String namePrefix;
}