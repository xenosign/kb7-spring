package org.example.kb7spring.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomIntegrityEvent {
    private String checkedAt;
    private int totalClassroomsChecked;
    private int violatedClassroomCount;
    private List<Violation> violations;

    public static ClassroomIntegrityEvent of(int totalClassroomsChecked, List<Violation> violations) {
        return new ClassroomIntegrityEvent(
                LocalDateTime.now().toString(),
                totalClassroomsChecked,
                violations.size(),
                violations
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Violation {
        private Long classroomId;
        private String roomName;
        private int capacity;
        private long actualCount;
        private long exceededBy;
    }
}
