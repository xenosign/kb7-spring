package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.service.StudentServiceV4;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@RequestMapping("/api/student/v4")
public class StudentApiControllerV4 {
    private final StudentServiceV4 studentService;

    // 1. 락 없이 동시 등록 - 정원 초과 재현용
    @GetMapping("/classroom/{classroomId}/race")
    public ResponseEntity<Map<String, Object>> enrollRace(
            @PathVariable Long classroomId,
            @RequestParam(defaultValue = "50") int requestCount,
            @RequestParam(defaultValue = "10") int capacity) throws InterruptedException {
        log.info("====================> StudentApiControllerV4 /enroll-race, classroomId={}, requestCount={}, capacity={}",
                classroomId, requestCount, capacity);

        return ResponseEntity.ok(studentService.enrollRace(classroomId, requestCount, capacity));
    }

    // 2. 낙관적 락(@Version) 적용
    @GetMapping("/classroom/{classroomId}/optimistic")
    public ResponseEntity<Map<String, Object>> enrollRaceOptimistic(
            @PathVariable Long classroomId,
            @RequestParam(defaultValue = "50") int requestCount,
            @RequestParam(defaultValue = "10") int capacity) throws InterruptedException {
        log.info("====================> StudentApiControllerV4 /enroll-race-optimistic, classroomId={}, requestCount={}, capacity={}",
                classroomId, requestCount, capacity);

        return ResponseEntity.ok(studentService.enrollRaceOptimistic(classroomId, requestCount, capacity));
    }

    // 3. 비관적 락(SELECT ... FOR UPDATE) 적용
    @GetMapping("/classroom/{classroomId}/pessimistic")
    public ResponseEntity<Map<String, Object>> enrollRacePessimistic(
            @PathVariable Long classroomId,
            @RequestParam(defaultValue = "50") int requestCount,
            @RequestParam(defaultValue = "10") int capacity) throws InterruptedException {
        log.info("====================> StudentApiControllerV4 /enroll-race-pessimistic, classroomId={}, requestCount={}, capacity={}",
                classroomId, requestCount, capacity);

        return ResponseEntity.ok(studentService.enrollRacePessimistic(classroomId, requestCount, capacity));
    }

    // 4. Redis 분산 락 적용 (마지막에 다룰 전략)
    @GetMapping("/classroom/{classroomId}/redis-lock")
    public ResponseEntity<Map<String, Object>> enrollRaceWithLock(
            @PathVariable Long classroomId,
            @RequestParam(defaultValue = "50") int requestCount,
            @RequestParam(defaultValue = "10") int capacity) throws InterruptedException {
        log.info("====================> StudentApiControllerV4 /enroll-race-lock, classroomId={}, requestCount={}, capacity={}",
                classroomId, requestCount, capacity);

        return ResponseEntity.ok(studentService.enrollRaceWithLock(classroomId, requestCount, capacity));
    }
}
