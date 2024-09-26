package org.example.xtimer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.xtimer.common.entity.Task;

import java.util.List;

@Mapper
public interface TaskMapper {
    void saveBatch(List<Task> taskList);

    Task getTaskByTimerIdAndUnix(Long timerId, Long unix);

    void update(Task task);

    List<Task> getTaskByTimeRange(long time, long time1);
}
