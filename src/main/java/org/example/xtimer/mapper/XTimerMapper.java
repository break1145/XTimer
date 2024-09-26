package org.example.xtimer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.xtimer.common.entity.XTimer;

import java.util.List;

@Mapper
public interface XTimerMapper {

    XTimer getTimerById(long id);

    void save(XTimer timer);

    void update(XTimer timer);

    List<XTimer> getTimersByStatus(int status);
}
