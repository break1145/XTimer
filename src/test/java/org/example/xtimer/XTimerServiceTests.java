package org.example.xtimer;

import org.example.xtimer.common.entity.XTimer;
import org.example.xtimer.exception.BusinessException;
import org.example.xtimer.exception.ErrorCode;
import org.example.xtimer.service.XTimerService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class XTimerServiceTests {

    @Autowired
    XTimerService xTimerService;

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
    @Transactional
    public void testCreateTimer_normalCase() {
        Long timerId = xTimerService.createTimer(timer);
        assertNotNull(timerId, "Timer ID should not be null");
    }

    @Test
    @Transactional
    public void testCreateTimer_inValidCron() {
        XTimer timer1 = new XTimer(timer); // 使用拷贝构造函数
        timer1.setCron("1212121212");

        Exception exception = assertThrows(BusinessException.class, () -> {
            xTimerService.createTimer(timer1);
        });

        String expectedMessage = "invalid cron";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), "Exception message should contain 'invalid cron'");
    }

    @Test
    @Transactional
    public void testCreateTimer_nullTimer() {
        Exception exception = assertThrows(BusinessException.class, () -> {
            xTimerService.createTimer(null);
        });

        String expectedMessage = ErrorCode.PARAMS_ERROR.getMessage();
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), "Exception message should contain PARAMS_ERROR");
    }
}
