package org.example.kb7spring.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.event.dto.ClassroomIntegrityEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassroomIntegrityEventPublisher {
    private final KafkaTemplate<String, ClassroomIntegrityEvent> classroomIntegrityEventKafkaTemplate;

    private final String classroomIntegrityEventsTopic = "classroom-integrity-events";

    public void publish(ClassroomIntegrityEvent event) {
        try {
            ListenableFuture<?> future = classroomIntegrityEventKafkaTemplate.send(classroomIntegrityEventsTopic, event);
            future.addCallback(
                    result -> log.debug("반 정합성 위반 이벤트 발행 성공 - 위반 {}건", event.getViolatedClassroomCount()),
                    ex -> log.warn("반 정합성 위반 이벤트 발행 실패 - Kafka 브로커 연결을 확인하세요", ex)
            );
        } catch (Exception e) {
            log.warn("반 정합성 위반 이벤트 발행 중 예외 발생", e);
        }
    }
}
