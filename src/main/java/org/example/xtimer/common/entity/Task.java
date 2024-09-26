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
public class Task extends BaseEntity implements Serializable {
    private Integer taskId;

    private String app;

    private Long timerId;

    private String output;

    private Long runTimer;

    private int costTime;

    private int status;
}
