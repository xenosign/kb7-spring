package org.example.kb7spring.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.event.dto.ErrorEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorEventPublisher {
    private final KafkaTemplate<String, ErrorEvent> errorEventKafkaTemplate;

    private final String errorEventsTopic = "error-events";

    public void publish(ErrorEvent event) {
        try {
            ListenableFuture<?> future = errorEventKafkaTemplate.send(errorEventsTopic, event);
            future.addCallback(
                    result -> log.debug("에러 이벤트 발행 성공 - {}", event.getExceptionType()),
                    ex -> log.warn("에러 이벤트 발행 실패 - Kafka 브로커 연결을 확인하세요", ex)
            );
        } catch (Exception e) {
            log.warn("에러 이벤트 발행 중 예외 발생", e);
        }
    }
}
