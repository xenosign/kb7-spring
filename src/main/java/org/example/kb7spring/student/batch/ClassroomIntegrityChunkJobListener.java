package org.example.kb7spring.student.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.event.dto.ClassroomIntegrityEvent;
import org.example.kb7spring.event.service.ClassroomIntegrityEventPublisher;
import org.example.kb7spring.student.repository.ClassroomRepository;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassroomIntegrityChunkJobListener implements JobExecutionListener {
    private final ClassroomIntegrityViolationAccumulator accumulator;
    private final ClassroomIntegrityEventPublisher classroomIntegrityEventPublisher;
    private final ClassroomRepository classroomRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        accumulator.reset();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        List<ClassroomIntegrityEvent.Violation> violations = accumulator.snapshot();
        long totalClassrooms = classroomRepository.count();

        if (violations.isEmpty()) {
            log.info("반 정합성 점검(chunk) 완료 - 전체 {}개 반, 위반 없음", totalClassrooms);
        } else {
            log.warn("반 정합성 점검(chunk) 완료 - 전체 {}개 반, 위반 {}건 발견", totalClassrooms, violations.size());
            classroomIntegrityEventPublisher.publish(
                    ClassroomIntegrityEvent.of((int) totalClassrooms, violations)
            );
        }

        accumulator.reset();
    }
}
