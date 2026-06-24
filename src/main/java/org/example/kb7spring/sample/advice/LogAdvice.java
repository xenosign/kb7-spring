package org.example.kb7spring.sample.advice;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class LogAdvice {
    @Before("execution(* org.example.kb7spring.sample.service.SampleService*.*(..))")
    public void logBefore() {
        log.info("==================================");
    }

    @Before("execution(* org.example.kb7spring.sample.service.SampleService*.doAdd(String, String)) && args(str1, str2)")
    public void logBeforeWithParam(String str1, String str2) {
        log.info("str1: " + str1);
        log.info("str2: " + str2);
    }
}