package org.example.xtimer.service.executor;


import lombok.extern.slf4j.Slf4j;
import org.example.xtimer.common.entity.NotifyHTTPParam;
import org.example.xtimer.common.entity.Task;
import org.example.xtimer.common.entity.XTimer;
import org.example.xtimer.common.enums.TaskStatus;
import org.example.xtimer.common.enums.TimerStatus;
import org.example.xtimer.exception.BusinessException;
import org.example.xtimer.exception.ErrorCode;
import org.example.xtimer.mapper.TaskMapper;
import org.example.xtimer.mapper.XTimerMapper;
import org.example.xtimer.utils.JSONUtil;
import org.example.xtimer.utils.TimerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class ExecutorWorker {

    @Autowired
    TaskMapper taskMapper;
    @Autowired
    XTimerMapper xTimerMapper;

    public ExecutorWorker(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public void work(String timerIDUnixKey) {

        List<Long> longs = TimerUtils.SplitTimerIDUnix(timerIDUnixKey);
        if (longs.size() != 2) {
            log.error("splitTimerIDUnix 错误, timerIDUnix:"+timerIDUnixKey);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"splitTimerIDUnix 错误, timerIDUnix:"+timerIDUnixKey);
        }
        Long timerId = longs.get(0);
        Long unix = longs.get(1);
        Task task = taskMapper.getTaskByTimerIdAndUnix(timerId, unix);
        if (task == null) {
            log.error("task获取失败：timerId="+timerId+", unix="+unix);
            return;
        }
        if (task.getStatus() != TaskStatus.NotRun.getStatus()) {
            log.warn("重复执行任务：timerId="+timerId+", unix="+unix+", status="+task.getStatus());
            return;
        }
        executeAndPostProcess(task, timerId, unix);
    }

    private void executeAndPostProcess(Task task, Long timerId, Long unix) {
        XTimer timer = xTimerMapper.getTimerById(timerId);
        if (timer == null) {
            log.error("执行回调错误，找不到对应的Timer。 timerId"+timerId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"执行回调错误，找不到对应的Timer。 timerId"+timerId);
        }
        if (timer.getStatus() != TimerStatus.Enable.getStatus()) {
            log.warn("Timer未处于激活状态。timerId="+timerId+", unix="+unix+", status="+timer.getStatus());
        }

        // 触发时间的误差时间
        int gapTime = (int) (new Date().getTime() - task.getRunTimer());
        task.setCostTime(gapTime);

        // 执行http回调，通知业务方
        ResponseEntity<String> response = null;
        try {
            response = executeTimerCallBack(timer);
        } catch (Exception e) {
            log.error("执行回调失败：e:"+ e.getMessage());
        }

        // 将Timer的执行结果保存到对应task中
        if (response == null) {
            task.setStatus(TaskStatus.Failed.getStatus());
            task.setOutput("response is null");
        } else if (response.getStatusCode().is2xxSuccessful()) {
            task.setStatus(TaskStatus.Succeed.getStatus());
            task.setOutput(response.toString());
        } else {
            task.setStatus(TaskStatus.Failed.getStatus());
            task.setOutput(response.toString());
        }
        taskMapper.update(task);
    }

    private ResponseEntity<String> executeTimerCallBack(XTimer timer) {
        NotifyHTTPParam httpParam = JSONUtil.parseObject(timer.getNotifyHTTPParam(), NotifyHTTPParam.class);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = null;

        if (httpParam == null) {
            log.error("httpParam is null");
            return null;
        }
        switch (httpParam.getMethod()){
            case "POST":
                response = restTemplate.postForEntity(httpParam.getUrl(), httpParam.getBody(),String.class);
            default:
                log.error("请求方法 " + httpParam.getMethod() + "暂未支持或不合法");
                break;
        }
        if (response == null) {
            log.error("response is null");
            return null;
        }
        HttpStatus status = (HttpStatus) response.getStatusCode();
        if (!status.is2xxSuccessful()) {
            log.error("回调失败" + response);
        }
        return response;
    }
}
