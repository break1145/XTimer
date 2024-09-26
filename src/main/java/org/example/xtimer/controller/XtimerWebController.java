package org.example.xtimer.controller;


import jdk.jshell.spi.ExecutionControl;
import org.example.xtimer.common.entity.Result;
import org.example.xtimer.common.entity.XTimer;
import org.example.xtimer.service.XTimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/xtimer")
public class XtimerWebController {

    @Autowired
    XTimerService service;

    @RequestMapping("/create/timer")
    public Result<Long> createTimer(@RequestBody XTimer timer) {
        Long id = service.createTimer(timer);
        return Result.ok(id);
    }

    @RequestMapping("/enable/timer")
    public Result<Long> enableTimer(@RequestParam(value = "app") String app,
                                    @RequestParam(value = "timerId") Long timerId,
                                    @RequestHeader MultiValueMap<String, String> headers) {
        service.enableTimer(app, timerId);
        return Result.ok();
    }

}
