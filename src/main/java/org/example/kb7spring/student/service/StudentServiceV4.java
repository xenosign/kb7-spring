package org.example.kb7spring.student.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.domain.Classroom;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.repository.ClassroomRepository;
import org.example.kb7spring.student.repository.StudentRepositoryV2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceV4 {
    private final StudentRepositoryV2 studentRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomLockService classroomLockService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int LOCK_WAIT_ATTEMPTS = 50;
    private static final long LOCK_WAIT_MILLIS = 20;
    private static final Duration LOCK_TTL = Duration.ofSeconds(3);
    private static final int OPTIMISTIC_MAX_RETRY = 30;

    @FunctionalInterface
    private interface EnrollAttempt {
        boolean attempt(Long classroomId) throws InterruptedException;
    }

    // 1. 락 없이 동시 등록 시도 - 정원 초과 재현
    public Map<String, Object> enrollRace(Long classroomId, int requestCount, int capacity) throws InterruptedException {
        return runSimulation(classroomId, requestCount, capacity, "NO_LOCK", this::tryEnroll);
    }

    // 2. 낙관적 락(@Version) - 충돌 시 재시도
    public Map<String, Object> enrollRaceOptimistic(Long classroomId, int requestCount, int capacity) throws InterruptedException {
        return runSimulation(classroomId, requestCount, capacity, "OPTIMISTIC_LOCK", this::tryEnrollOptimistic);
    }

    // 3. 비관적 락(SELECT ... FOR UPDATE) - 한 번에 하나씩 순차 처리
    public Map<String, Object> enrollRacePessimistic(Long classroomId, int requestCount, int capacity) throws InterruptedException {
        return runSimulation(classroomId, requestCount, capacity, "PESSIMISTIC_LOCK", classroomLockService::enrollPessimistic);
    }

    // 4. Redis 분산 락 - 여러 서버 인스턴스를 가정한 애플리케이션 레벨 락
    public Map<String, Object> enrollRaceWithLock(Long classroomId, int requestCount, int capacity) throws InterruptedException {
        return runSimulation(classroomId, requestCount, capacity, "REDIS_LOCK", this::tryEnrollWithLock);
    }

    private Map<String, Object> runSimulation(Long classroomId, int requestCount, int capacity,
                                               String strategy, EnrollAttempt attempt) throws InterruptedException {
        // 매번 동일한 조건에서 재현하기 위해 정원/카운터를 설정하고 기존 등록 학생을 초기화
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반입니다: " + classroomId));
        classroom.setCapacity(capacity);
        classroom.setStudentCount(0);
        classroomRepository.save(classroom);
        studentRepository.deleteByClassroomId(classroomId);

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(requestCount, 50));
        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    if (attempt.attempt(classroomId)) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        long actualEnrolledCount = studentRepository.countByClassroomId(classroomId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("strategy", strategy);
        result.put("capacity", capacity);
        result.put("requestCount", requestCount);
        result.put("successCount", successCount.get());
        result.put("actualEnrolledCount", actualEnrolledCount);
        result.put("overCapacity", actualEnrolledCount > capacity);
        return result;
    }

    // 정원 확인 후 등록 - 확인과 등록 사이에 다른 스레드가 끼어들 수 있음 (race condition)
    private boolean tryEnroll(Long classroomId) {
        long currentCount = studentRepository.countByClassroomId(classroomId);
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow((() -> new IllegalArgumentException("존재하지 않는 반입니다: " + classroomId)));
        if (currentCount >= classroom.getCapacity()) {
            return false;
        }

        Student student = new Student();
        student.setName("신청자-" + System.nanoTime());
        student.setClassroom(classroom);
        studentRepository.save(student);
        return true;
    }

    // 낙관적 락 - 버전 충돌(ObjectOptimisticLockingFailureException) 시 재시도
    private boolean tryEnrollOptimistic(Long classroomId) {
        for (int attempt = 0; attempt < OPTIMISTIC_MAX_RETRY; attempt++) {
            try {
                return classroomLockService.enrollOptimisticOnce(classroomId);
            } catch (ObjectOptimisticLockingFailureException e) {
                // 다른 스레드가 먼저 커밋함 - 최신 상태로 다시 읽어서 재시도
            }
        }
        log.info("낙관적 락 재시도 초과 - 등록 포기");
        return false;
    }

    // Redis 분산 락으로 확인+등록을 한 번에 하나의 스레드만 수행하도록 강제
    private boolean tryEnrollWithLock(Long classroomId) throws InterruptedException {
        String lockKey = "lock:classroom:" + classroomId;
        boolean locked = false;
        try {
            for (int attempt = 0; attempt < LOCK_WAIT_ATTEMPTS && !locked; attempt++) {
                locked = Boolean.TRUE.equals(
                        redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", LOCK_TTL));
                if (!locked) {
                    Thread.sleep(LOCK_WAIT_MILLIS);
                }
            }
            if (!locked) {
                log.info("락 획득 실패 - 등록 포기");
                return false;
            }
            return tryEnroll(classroomId);
        } finally {
            if (locked) {
                redisTemplate.delete(lockKey);
            }
        }
    }
}
