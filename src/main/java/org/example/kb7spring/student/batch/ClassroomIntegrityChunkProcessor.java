package org.example.kb7spring.student.batch;

import lombok.RequiredArgsConstructor;
import org.example.kb7spring.event.dto.ClassroomIntegrityEvent;
import org.example.kb7spring.student.domain.Classroom;
import org.example.kb7spring.student.repository.StudentRepositoryV2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

// classroom 을 페이징으로 하나씩 읽는 동안 매번 count 쿼리(N+1)를 날리지 않도록,
// Step 시작 시점에 전체 classroom_id -> 학생 수를 단 한 번의 GROUP BY 쿼리로 미리 읽어 메모리에 올려둔다.
@Component
@RequiredArgsConstructor
public class ClassroomIntegrityChunkProcessor
        implements ItemProcessor<Classroom, ClassroomIntegrityEvent.Violation>, StepExecutionListener {
    private final StudentRepositoryV2 studentRepository;

    private Map<Long, Long> studentCountByClassroomId = Collections.emptyMap();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        studentCountByClassroomId = studentRepository.countGroupByClassroomId().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }

    @Override
    public ClassroomIntegrityEvent.Violation process(Classroom classroom) {
        long actualCount = studentCountByClassroomId.getOrDefault(classroom.getId(), 0L);
        if (actualCount <= classroom.getCapacity()) {
            return null;
        }
        return new ClassroomIntegrityEvent.Violation(
                classroom.getId(),
                classroom.getRoomName(),
                classroom.getCapacity(),
                actualCount,
                actualCount - classroom.getCapacity()
        );
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        studentCountByClassroomId = Collections.emptyMap();
        return stepExecution.getExitStatus();
    }
}
