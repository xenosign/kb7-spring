package org.example.kb7spring.student.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassroomIntegrityScheduler {
    private final JobLauncher jobLauncher;
    private final Job classroomIntegrityCheckChunkJob;

    // 매일 새벽 3시에 반 정원 정합성 점검 배치를 실행한다.
    // classroom/student 가 계속 늘어날 걸 감안해 페이징+청크 기반 Job을 사용한다.
    @Scheduled(cron = "0 * * * * *")
    public void runClassroomIntegrityCheck() {
        try {
            // JobParameters 가 이전 실행과 완전히 같으면 Spring Batch 가 재실행을 거부하므로
            // 실행 시각을 파라미터로 넣어 매번 새로운 JobInstance 가 되도록 한다.
            jobLauncher.run(classroomIntegrityCheckChunkJob,
                    new JobParametersBuilder()
                            .addString("runAt", LocalDateTime.now().toString())
                            .toJobParameters());
        } catch (Exception e) {
            log.error("반 정합성 점검 배치 실행 실패", e);
        }
    }
}
