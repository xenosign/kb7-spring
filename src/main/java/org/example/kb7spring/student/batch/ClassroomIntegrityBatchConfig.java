package org.example.kb7spring.student.batch;

import lombok.RequiredArgsConstructor;
import org.example.kb7spring.event.dto.ClassroomIntegrityEvent;
import org.example.kb7spring.student.domain.Classroom;
import org.example.kb7spring.student.repository.ClassroomRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ClassroomIntegrityBatchConfig {
    // 페이지(청크) 당 읽어올 classroom 건수. 3,000개 반 기준 15페이지로 나뉜다.
    private static final int CHUNK_SIZE = 200;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ClassroomRepository classroomRepository;
    private final ClassroomIntegrityCheckTasklet classroomIntegrityCheckTasklet;
    private final ClassroomIntegrityChunkProcessor classroomIntegrityChunkProcessor;
    private final ClassroomIntegrityChunkWriter classroomIntegrityChunkWriter;
    private final ClassroomIntegrityChunkJobListener classroomIntegrityChunkJobListener;

    // ------------------------------------------------------------------
    // 비교용 baseline: findAll() 로 전체 classroom 을 한 번에 메모리에 올리고
    // classroom 마다 count 쿼리를 개별로 날리는(N+1) "제대로 배치하지 않은" 버전.
    // ------------------------------------------------------------------
    @Bean
    public Job classroomIntegrityCheckJob() {
        return jobBuilderFactory.get("classroomIntegrityCheckJob")
                .start(classroomIntegrityCheckStep())
                .build();
    }

    @Bean
    public Step classroomIntegrityCheckStep() {
        return stepBuilderFactory.get("classroomIntegrityCheckStep")
                .tasklet(classroomIntegrityCheckTasklet)
                .build();
    }

    // ------------------------------------------------------------------
    // 페이징 + 청크 기반 버전: classroom 을 CHUNK_SIZE 단위로 나눠서 읽고,
    // 학생 수는 GROUP BY 로 한 번만 집계해서 N+1 을 없앤 "제대로 배치한" 버전.
    // ------------------------------------------------------------------
    @Bean
    public Job classroomIntegrityCheckChunkJob() {
        return jobBuilderFactory.get("classroomIntegrityCheckChunkJob")
                .listener(classroomIntegrityChunkJobListener)
                .start(classroomIntegrityCheckChunkStep())
                .build();
    }

    @Bean
    public Step classroomIntegrityCheckChunkStep() {
        return stepBuilderFactory.get("classroomIntegrityCheckChunkStep")
                .<Classroom, ClassroomIntegrityEvent.Violation>chunk(CHUNK_SIZE)
                .reader(classroomPagingReader())
                .processor(classroomIntegrityChunkProcessor)
                .writer(classroomIntegrityChunkWriter)
                .listener(classroomIntegrityChunkProcessor)
                .build();
    }

    @Bean
    public RepositoryItemReader<Classroom> classroomPagingReader() {
        RepositoryItemReader<Classroom> reader = new RepositoryItemReader<>();
        reader.setRepository(classroomRepository);
        reader.setMethodName("findAll");
        reader.setPageSize(CHUNK_SIZE);

        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.ASC);
        reader.setSort(sorts);

        return reader;
    }
}
