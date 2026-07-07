package org.example.kb7spring.event.consumer;

import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.event.dto.ErrorEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class SlackNotifierConsumer {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${slack.webhook-url}")
    private String webhookUrl;

    @KafkaListener(
            topics = "error-events",
            groupId = "error-slack-group",
            containerFactory = "errorEventListenerContainerFactory"
    )
    public void consume(ErrorEvent event) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.warn("slack.webhook-url 미설정 - Slack 알림을 건너뜁니다");
            return;
        }

        try {
            String text = String.format(
                    "*[서버 에러 발생]*\n- 위치: `%s`\n- 예외: `%s`\n- 메시지: %s\n- 시각: %s",
                    event.getSource(), event.getExceptionType(), event.getMessage(), event.getOccurredAt()
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
