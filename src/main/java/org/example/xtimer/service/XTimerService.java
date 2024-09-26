package org.example.xtimer.service;

import org.example.xtimer.common.entity.XTimer;

public interface XTimerService {
    Long createTimer(XTimer timer);

    void enableTimer(String app, Long timerId);

    void UnEnableTimer(String app, long id);
}
