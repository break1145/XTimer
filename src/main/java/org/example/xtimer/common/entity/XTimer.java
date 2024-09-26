package org.example.xtimer.common.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XTimer extends BaseEntity implements Serializable {
    private Long timerId;

    private String app;

    private String name;

    private int status;

    private String cron;

    private String notifyHTTPParam;

    public XTimer(XTimer timer) {
        this.app = timer.getApp();
        this.name = timer.getName();
        this.status = timer.getStatus();
        this.cron = timer.getCron();
        this.notifyHTTPParam = timer.getNotifyHTTPParam();
    }
}
