package org.example.kb7spring.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.kb7spring.event.dto.ClassroomIntegrityEvent;
import org.example.kb7spring.event.dto.ErrorEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {


    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, ErrorEvent> errorEventProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, ErrorEvent> errorEventKafkaTemplate() {
        return new KafkaTemplate<>(errorEventProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, ErrorEvent> errorEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "org.example.kb7spring.event.dto");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ErrorEvent.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    // 로그 적재 컨슈머, Slack 알림 컨슈머가 같은 팩토리를 쓰되 groupId를 각자 지정해서
    // 같은 토픽을 서로 영향 없이 독립적으로 구독(fan-out)하게 한다.
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ErrorEvent> errorEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ErrorEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(errorEventConsumerFactory());
        return factory;
    }

    // Classroom 정합성 점검 배치가 발행하는 전용 토픽(classroom-integrity-events) 용 빈들
    @Bean
    public ProducerFactory<String, ClassroomIntegrityEvent> classroomIntegrityEventProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, ClassroomIntegrityEvent> classroomIntegrityEventKafkaTemplate() {
        return new KafkaTemplate<>(classroomIntegrityEventProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, ClassroomIntegrityEvent> classroomIntegrityEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "org.example.kb7spring.event.dto");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ClassroomIntegrityEvent.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClassroomIntegrityEvent> classroomIntegrityEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ClassroomIntegrityEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(classroomIntegrityEventConsumerFactory());
        return factory;
    }
}
