package org.example.xtimer;

import org.example.xtimer.common.conf.MigratorAppConf;
import org.example.xtimer.common.entity.Task;
import org.example.xtimer.common.entity.XTimer;
import org.example.xtimer.exception.BusinessException;
import org.example.xtimer.exception.ErrorCode;
import org.example.xtimer.manager.impl.MigratorManagerImpl;
import org.example.xtimer.mapper.TaskMapper;
import org.example.xtimer.utils.TimerUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class TaskMapperTests {
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private MigratorManagerImpl migratorManager;
    @Autowired
    private MigratorAppConf migratorAppConf;

    private static XTimer timer;
    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        timer = new XTimer(); // 初始化 timer 实例
        timer.setName("XTimer");
        timer.setApp("app");
        timer.setStatus(1);
        timer.setCron("0 0 12 ? * FRI");
        timer.setNotifyHTTPParam("???");
    }
    @Test
    public void testBasic_saveBatch() {
        CronExpression cronExpression;
        try {
            cronExpression = new CronExpression(timer.getCron());
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析cron表达式失败："+timer.getCron());
        }
        Date now = new Date();
        Date end = TimerUtils.GetForwardTwoMigrateStepEnd(now,migratorAppConf.getMigrateStepMinutes());
        List<Long> executeTimes = TimerUtils.GetCronNextsBetween(cronExpression,now,end);


        List<Task> taskList = migratorManager.batchTasksFromTimer(timer,executeTimes);
        taskMapper.saveBatch(taskList);
    }
}
