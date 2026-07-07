package org.example.kb7spring.student.batch;

import lombok.RequiredArgsConstructor;
import org.example.kb7spring.event.dto.ClassroomIntegrityEvent;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ClassroomIntegrityChunkWriter implements ItemWriter<ClassroomIntegrityEvent.Violation> {
    private final ClassroomIntegrityViolationAccumulator accumulator;

    @Override
    public void write(List<? extends ClassroomIntegrityEvent.Violation> items) {
        accumulator.addAll(items);
    }
}
