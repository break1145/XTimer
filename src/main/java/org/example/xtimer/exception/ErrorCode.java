package org.example.xtimer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "ok"),
    UNKNOWN_ERROR(90001, "未知异常"),
    SYSTEM_ERROR(90002, "系统内部异常"),
    PARAMS_ERROR(90003, "请求参数错误"),

    ;

    private final int code;
    private final String message;

}