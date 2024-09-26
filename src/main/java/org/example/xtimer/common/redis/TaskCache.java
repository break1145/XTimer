package org.example.xtimer.common.redis;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.xtimer.common.conf.SchedulerAppConf;
import org.example.xtimer.common.entity.Task;
import org.example.xtimer.exception.BusinessException;
import org.example.xtimer.exception.ErrorCode;
import org.example.xtimer.utils.TimerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class TaskCache {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    SchedulerAppConf schedulerAppConf;

    public String GetTableName(Task task){
        int maxBucket = schedulerAppConf.getBucketsNum();

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(new Date(task.getRunTimer()));
        long index = task.getTimerId()%maxBucket;
        return sb.append(timeStr).append("_").append(index).toString();
    }

    /**
     * 接收一个任务列表，并将这些任务以有序集合（ZSet）的形式存储到 Redis 中。
     * @param taskList 任务列表
     * @return 成功则返回true，否则false
     */
    public boolean cacheSaveTasks(List<Task> taskList){
        try {
            SessionCallback sessionCallback = new SessionCallback() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    for (Task task : taskList) {
                        Long unix = task.getRunTimer();
                        String tableName = GetTableName(task);
                        redisTemplate.opsForZSet().add(
                                tableName,
                                TimerUtils.UnionTimerIDUnix(task.getTimerId(), unix),
                                unix
                        );
                    }
                    return operations.exec();
                }
            };
            redisTemplate.execute(sessionCallback);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 从 Redis 的有序集合中，基于 score（即任务的 runTimer）的范围，检索出任务列表。
     * @param key
     * @param start 时间范围
     * @param end 时间范围
     * @return 任务列表
     */
    public List<Task> getTasksFromCache(String key,long start, long end){
        List<Task> taskList = new ArrayList<>();
        Set<Object> timerIDUnixs = redisTemplate.opsForZSet().rangeByScore(key,start,end);
        if (CollectionUtils.isEmpty(timerIDUnixs)) {
            return taskList;
        }

        for (Object timerIDUnixObj : timerIDUnixs) {
            Task task = new Task();
            String timerIDUnix = (String)timerIDUnixObj;
            List<Long> longSet = TimerUtils.SplitTimerIDUnix(timerIDUnix);
            if(longSet.size() != 2){
                log.error("splitTimerIDUnix 错误, timerIDUnix:"+timerIDUnix);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"splitTimerIDUnix 错误, timerIDUnix:"+timerIDUnix);
            }
            task.setTimerId(longSet.get(0));
            task.setRunTimer(longSet.get(1));
            taskList.add(task);
        }

        return taskList;
    }

}
