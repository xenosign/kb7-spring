package org.example.kb7spring.event.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/event")
public class ErrorTestController {

    @GetMapping("/error-test")
    public String errorTest() {
        log.info("500 에러 강제 발생 테스트 요청");
        throw new RuntimeException("Kafka -> Slack 알림 테스트용 강제 에러");
    }
}
