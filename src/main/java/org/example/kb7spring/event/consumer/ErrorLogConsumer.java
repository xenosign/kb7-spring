package org.example.kb7spring.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.event.domain.ErrorLog;
import org.example.kb7spring.event.dto.ErrorEvent;
import org.example.kb7spring.event.repository.ErrorLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorLogConsumer {
    private final ErrorLogRepository errorLogRepository;

    @KafkaListener(
            topics = "${kafka.topic.error-events}",
            groupId = "error-log-group",
            containerFactory = "errorEventListenerContainerFactory"
    )
    public void consume(ErrorEvent event) {
        errorLogRepository.save(ErrorLog.from(event));
        log.info("에러 로그 적재 완료 - {}: {}", event.getExceptionType(), event.getMessage());
    }
}
