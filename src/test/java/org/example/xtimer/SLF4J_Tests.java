package org.example.xtimer;
import org.example.xtimer.common.entity.XTimer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class SLF4J_Tests {


    @Test
    public void test() {
        final Logger logger = LoggerFactory.getLogger(getClass());
        int score = 99;
        XTimer xTimer = new XTimer();
        xTimer.setName("test timer name");

        logger.info("我要往文件里输入一行日志了{}", xTimer);
    }
}



