package org.example.kb7spring.sample.service;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.config.RootConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class })
@WebAppConfiguration
@Slf4j
class SampleServiceImplTest {
    @Autowired
    private SampleService service;
    @Test
    public void doAdd() throws Exception {
        log.info("{}", service.doAdd("123", "456"));
    }
}