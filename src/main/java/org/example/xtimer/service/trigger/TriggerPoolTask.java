package org.example.xtimer.service.trigger;


import lombok.extern.slf4j.Slf4j;
import org.example.xtimer.common.entity.Task;
import org.example.xtimer.utils.TimerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.example.xtimer.service.executor.ExecutorWorker;

@Component
@Slf4j
public class TriggerPoolTask {

    @Autowired
    ExecutorWorker executorWorker;

    public void runExecutor(Task task) {
        if(task == null){
            return;
        }
        log.info("start runExecutor");

        executorWorker.work(TimerUtils.UnionTimerIDUnix(task.getTimerId(),task.getRunTimer()));

        log.info("end executeAsync");
    }
}
