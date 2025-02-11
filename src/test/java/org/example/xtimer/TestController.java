package org.example.xtimer;

import lombok.extern.slf4j.Slf4j;
import org.example.xtimer.common.entity.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/xtimer")
@Slf4j
public class TestController {

    @PostMapping("/callback")
    public ResponseEntity<String> callback(@RequestBody String callbackInfo) {
        log.info("CALLBACK:"+callbackInfo);
        // 消息队列发送消息
        return ResponseEntity.ok(
                "ok"
        );
    }
}

