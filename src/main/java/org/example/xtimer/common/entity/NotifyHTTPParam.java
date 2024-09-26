package org.example.xtimer.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NotifyHTTPParam {
    private String method;
    private String url;
    private Map<String,String> header;
    private String body;

}
