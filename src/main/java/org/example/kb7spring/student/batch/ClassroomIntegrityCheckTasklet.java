package org.example.kb7spring.student.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.event.dto.ClassroomIntegrityEvent;
import org.example.kb7spring.event.service.ClassroomIntegrityEventPublisher;
import org.example.kb7spring.student.domain.Classroom;
import org.example.kb7spring.student.repository.ClassroomRepository;
import org.example.kb7spring.student.repository.StudentRepositoryV2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// 반(Classroom)마다 실제 등록 인원(student 테이블 count)이 정원(capacity)을 넘었는지 전수 검사한다.
// race condition(동시 등록 시 정원 체크 후 insert 사이의 TOCTOU)으로 정원을 초과한 반이 있으면
// 전용 토픽으로 위반 내역을 발행해 Slack 알림을 트리거한다.
// classroom 전체를 findAll() 로 한 번에 메모리에 올리고 반마다 count 쿼리를 개별로(N+1) 날리는
// 버전으로, 실제 운영 스케줄러(ClassroomIntegrityScheduler)는 더 이상 이 Job을 쓰지 않는다.
// classroom/student 가 많아졌을 때 페이징+청크 기반 Job(ClassroomIntegrityCheckChunkJob)과
// 성능을 비교하기 위한 baseline 으로만 남겨둔다.
@Slf4j
@Component
@RequiredArgsConstructor
public class ClassroomIntegrityCheckTasklet implements Tasklet {
    private final ClassroomRepository classroomRepository;
    private final StudentRepositoryV2 studentRepository;
    private final ClassroomIntegrityEventPublisher classroomIntegrityEventPublisher;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<Classroom> classrooms = classroomRepository.findAll();
        List<ClassroomIntegrityEvent.Violation> violations = new ArrayList<>();

        for (Classroom classroom : classrooms) {
            long actualCount = studentRepository.countByClassroomId(classroom.getId());
            if (actualCount > classroom.getCapacity()) {
                violations.add(new ClassroomIntegrityEvent.Violation(
                        classroom.getId(),
                        classroom.getRoomName(),
                        classroom.getCapacity(),
                        actualCount,
                        actualCount - classroom.getCapacity()
                ));
            }
        }

        if (violations.isEmpty()) {
            log.info("반 정합성 점검 완료 - 전체 {}개 반, 위반 없음", classrooms.size());
        } else {
            log.warn("반 정합성 점검 완료 - 전체 {}개 반, 위반 {}건 발견", classrooms.size(), violations.size());
            classroomIntegrityEventPublisher.publish(
                    ClassroomIntegrityEvent.of(classrooms.size(), violations)
            );
        }

        // 점검 결과를 JobExecutionContext 에 담아 컨트롤러가 응답으로 그대로 돌려줄 수 있게 한다.
        ExecutionContext jobContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        jobContext.put("totalClassroomsChecked", classrooms.size());
        jobContext.put("violatedClassroomCount", violations.size());
        jobContext.put("violations", violations);

        return RepeatStatus.FINISHED;
    }
}
