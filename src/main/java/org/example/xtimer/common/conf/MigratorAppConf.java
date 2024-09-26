package org.example.xtimer.common.conf;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class MigratorAppConf {

    @Value("${migrator.workersNum}")
    private int workersNum;
    @Value("${migrator.migrateStepMinutes}")
    private int migrateStepMinutes;
    @Value("${migrator.migrateSuccessExpireMinutes}")
    private int migrateSuccessExpireMinutes;
    @Value("${migrator.migrateTryLockMinutes}")
    private int migrateTryLockMinutes;
    @Value("${migrator.timerDetailCacheMinutes}")
    private int timerDetailCacheMinutes;

}
