package org.example.kb7spring.student.batch;

import org.example.kb7spring.event.dto.ClassroomIntegrityEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 청크 기반 Step 은 writer 가 청크(페이지)마다 나눠서 호출되므로, 전체 Job 실행 동안의
// 위반 내역을 한 곳에 모아뒀다가 afterJob 에서 요약 이벤트 하나로 발행하기 위한 누적기.
// Job 이 동시에 여러 개 실행되지 않는(JobLauncher 가 동기 실행) 전제 하에 단순 싱글턴으로 둔다.
@Component
public class ClassroomIntegrityViolationAccumulator {
    private final List<ClassroomIntegrityEvent.Violation> violations = Collections.synchronizedList(new ArrayList<>());

    public void reset() {
        violations.clear();
    }

    public void addAll(List<? extends ClassroomIntegrityEvent.Violation> items) {
        violations.addAll(items);
    }

    public List<ClassroomIntegrityEvent.Violation> snapshot() {
        return new ArrayList<>(violations);
    }
}
