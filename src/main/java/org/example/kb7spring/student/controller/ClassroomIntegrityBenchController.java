package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// classroom/student 데이터를 IndexDemoController 의 /0/{classroomCount}/{capacity} 로 시딩한 뒤,
// 같은 데이터로 "제대로 배치하지 않은" Job과 "페이징+청크 기반" Job의 실행 시간을 비교하기 위한 컨트롤러.
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch/classroom-integrity")
public class ClassroomIntegrityBenchController {
    private final JobLauncher jobLauncher;
    private final Job classroomIntegrityCheckJob;
    private final Job classroomIntegrityCheckChunkJob;

    /**
     * [GET] /api/batch/classroom-integrity/full-load
     * findAll() 로 전체 classroom 을 한 번에 읽고, classroom 마다 count 쿼리를 개별로(N+1) 날리는 baseline Job 실행
     */
    @GetMapping("/full-load")
    public Map<String, Object> runFullLoad() throws Exception {
        return runAndMeasure("full-load", classroomIntegrityCheckJob);
    }

    /**
     * [GET] /api/batch/classroom-integrity/chunk
     * classroom 을 페이지 단위로 읽고, 학생 수는 GROUP BY 로 한 번만 집계하는 청크 기반 Job 실행
     */
    @GetMapping("/chunk")
    public Map<String, Object> runChunk() throws Exception {
        return runAndMeasure("chunk", classroomIntegrityCheckChunkJob);
    }

    private Map<String, Object> runAndMeasure(String label, Job job) throws Exception {
        long start = System.currentTimeMillis();

        // JobParameters 가 이전 실행과 완전히 같으면 재실행이 거부되므로 실행 시각을 파라미터로 넣는다.
        JobExecution execution = jobLauncher.run(job,
                new JobParametersBuilder()
                        .addString("runAt", LocalDateTime.now().toString())
                        .toJobParameters());

        long elapsed = System.currentTimeMillis() - start;
        log.info("[{}] Job 실행 완료 - status={}, elapsed={}ms", label, execution.getStatus(), elapsed);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("label", label);
        response.put("status", execution.getStatus().toString());
        response.put("elapsedMs", elapsed);
        return response;
    }
}
