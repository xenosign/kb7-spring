package org.example.kb7spring.event.consumer;

import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.event.dto.ClassroomIntegrityEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ClassroomIntegritySlackNotifier {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${slack.webhook-url}")
    private String webhookUrl;

    @KafkaListener(
            topics = "classroom-integrity-events",
            groupId = "classroom-integrity-slack-group",
            containerFactory = "classroomIntegrityEventListenerContainerFactory"
    )
    public void consume(ClassroomIntegrityEvent event) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.warn("slack.webhook-url 미설정 - Slack 알림을 건너뜁니다");
            return;
        }

        try {
            String violationLines = event.getViolations().stream()
                    .map(v -> String.format(
                            "  - `%s`(id=%d): 정원 %d명 / 실제 %d명 (초과 %d명)",
                            v.getRoomName(), v.getClassroomId(), v.getCapacity(), v.getActualCount(), v.getExceededBy()
                    ))
                    .collect(Collectors.joining("\n"));

            String text = String.format(
                    "*[반 정원 정합성 위반 발견]*\n- 점검 시각: %s\n- 전체 점검 반 수: %d\n- 위반 반 수: %d\n%s",
                    event.getCheckedAt(), event.getTotalClassroomsChecked(), event.getViolatedClassroomCount(), violationLines
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> payload = Collections.singletonMap("text", text);

            restTemplate.postForEntity(webhookUrl, new HttpEntity<>(payload, headers), String.class);
        } catch (Exception e) {
            log.error("Slack 알림 전송 실패", e);
        }
    }
}
