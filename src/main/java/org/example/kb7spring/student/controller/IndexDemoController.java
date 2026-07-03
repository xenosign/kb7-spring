package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * - 더미 데이터 10만 건 기준으로 인덱스 유무에 따른 실행 계획/성능 차이를 확인
 * - 각 단계는 EXPLAIN 실행 계획과 함께, 실제 쿼리를 BENCH_RUNS회 수행한 평균 소요 시간 확인
 * - 브라우저/Postman에서 바로 호출할 수 있도록 모든 엔드포인트를 GET 으로 통일
 * - 응답은 문자열이 아닌 JSON 구조(Map/List)로 반환
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/index/v1")
public class IndexDemoController {

    private final JdbcTemplate jdbcTemplate;

    /** 더미 데이터 건수 (인덱스 효과를 극적으로 보기 위해 10만 건) */
    private static final int SAMPLE_SIZE = 100_000;

    /** 실제 쿼리 반복 수행 횟수 (평균 시간 측정용) */
    private static final int BENCH_RUNS = 10;

    private static final String SAMPLE_NAME = "학생50001";
    private static final String SAMPLE_ROLE = "수강생";

    /**
     * EXPLAIN 결과를 로그에 남기고, 실행 계획을 줄 단위 리스트로 반환한다.
     * -> JSON 배열로 직렬화되어 Postman 에서 한 줄씩 깔끔하게 보인다.
     */
    private List<String> explain(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("EXPLAIN " + sql, args);
        List<String> plan = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String line = String.format("type=%s, key=%s, rows=%s, Extra=%s",
                    row.get("type"), row.get("key"), row.get("rows"), row.get("Extra"));
            log.info(line);
            plan.add(line);
        }
        return plan;
    }

    /**
     * 실제 쿼리를 BENCH_RUNS회 수행하고 평균 소요 시간을 구조화된 Map 으로 반환
     */
    private Map<String, Object> benchmark(String sql, Object... args) {
        // warm-up 1회 (디스크 I/O -> 버퍼 풀 적재로 인한 첫 실행 왜곡 제거)
        jdbcTemplate.queryForList(sql, args);

        long totalNanos = 0;
        int resultCount = 0;
        for (int i = 0; i < BENCH_RUNS; i++) {
            long start = System.nanoTime();
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
            totalNanos += System.nanoTime() - start;
            resultCount = rows.size();
        }
        double avgMs = totalNanos / (double) BENCH_RUNS / 1_000_000.0;
        log.info("실제 수행 {}회 평균: {}ms (조회 결과 {}건)",
                BENCH_RUNS, String.format("%.3f", avgMs), resultCount);

        Map<String, Object> bench = new LinkedHashMap<>();
        bench.put("runs", BENCH_RUNS);
        bench.put("avgMs", Math.round(avgMs * 1000) / 1000.0); // 소수점 3자리
        bench.put("resultCount", resultCount);
        return bench;
    }

    /**
     * 제목 + EXPLAIN 실행 계획 + 벤치마크 결과를 하나의 JSON 응답으로 조립한다.
     */
    private Map<String, Object> explainAndBench(String title, String sql, Object... args) {
        log.info(title);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("title", title);
        response.put("plan", explain(sql, args));
        response.put("benchmark", benchmark(sql, args));
        return response;
    }

    // 인덱스 생성/삭제처럼 단일 메시지만 돌려주는 응답용 편의 메서드
    private Map<String, Object> message(String msg) {
        log.info(msg);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", msg);
        return response;
    }

    /**
     * [GET] /api/index/v1/0/{classroomCount}
     * 0. 더미 데이터 대량 생성 - 건별 INSERT (update 반복)
     *    - classroom 먼저 재시딩 후, 그 범위 내에서 classroom_id 랜덤 배정
     */
    @GetMapping("/0/{classroomCount}")
    public Map<String, Object> seedV1(@PathVariable int classroomCount) {
        long start = System.currentTimeMillis();

        // classroom 재시딩 (FK 참조 무결성을 위해 student보다 먼저 초기화)
        jdbcTemplate.execute("DELETE FROM student");
        jdbcTemplate.execute("DELETE FROM classroom");
        jdbcTemplate.execute("ALTER TABLE classroom AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE student AUTO_INCREMENT = 1");

        List<Object[]> classroomBatch = new ArrayList<>(classroomCount);
        for (int i = 1; i <= classroomCount; i++) {
            classroomBatch.add(new Object[]{"강의실" + i});
        }
        jdbcTemplate.batchUpdate("INSERT INTO classroom (room_name) VALUES (?)", classroomBatch);

        // student 건별 INSERT
        String sql = "INSERT INTO student (name, role, specialty, status, classroom_id) VALUES (?, ?, ?, ?, ?)";
        String[] roles = {"수강생", "조교", "매니저"};
        String[] specialties = {"백엔드", "프론트엔드", "풀스택"};
        Random random = new Random();

        for (int i = 1; i <= SAMPLE_SIZE; i++) {
            jdbcTemplate.update(sql,
                    "학생" + i,
                    roles[i % roles.length],
                    specialties[i % specialties.length],
                    "ACTIVE",
                    random.nextInt(classroomCount) + 1);
        }

        long elapsed = System.currentTimeMillis() - start;
        return message("[건별 INSERT] classroom " + classroomCount + "개 + student " + SAMPLE_SIZE + "건 생성 완료 (" + elapsed + "ms)");
    }

    /**
     * [GET] /api/index/v1/0-1/{classroomCount}
     * 0-1. 더미 데이터 대량 생성 - 배치 INSERT (batchUpdate)
     *      - classroom 먼저 재시딩 후, 그 범위 내에서 classroom_id 랜덤 배정
     */
    @GetMapping("/0-1/{classroomCount}")
    public Map<String, Object> seedV2(@PathVariable int classroomCount) {
        long start = System.currentTimeMillis();

        // classroom 재시딩 (FK 참조 무결성을 위해 student보다 먼저 초기화)
        jdbcTemplate.execute("DELETE FROM student");
        jdbcTemplate.execute("DELETE FROM classroom");
        jdbcTemplate.execute("ALTER TABLE classroom AUTO_INCREMENT = 1");
        jdbcTemplate.execute("ALTER TABLE student AUTO_INCREMENT = 1");

        List<Object[]> classroomBatch = new ArrayList<>(classroomCount);
        for (int i = 1; i <= classroomCount; i++) {
            classroomBatch.add(new Object[]{"강의실" + i});
        }
        jdbcTemplate.batchUpdate("INSERT INTO classroom (room_name) VALUES (?)", classroomBatch);

        // student 배치 INSERT
        String sql = "INSERT INTO student (name, role, specialty, status, classroom_id) VALUES (?, ?, ?, ?, ?)";
        String[] roles = {"수강생", "조교", "매니저"};
        String[] specialties = {"백엔드", "프론트엔드", "풀스택"};
        Random random = new Random();

        int batchSize = 1000;
        List<Object[]> studentBatch = new ArrayList<>(batchSize);
        for (int i = 1; i <= SAMPLE_SIZE; i++) {
            studentBatch.add(new Object[]{
                    "학생" + i,
                    roles[i % roles.length],
                    specialties[i % specialties.length],
                    "ACTIVE",
                    random.nextInt(classroomCount) + 1
            });
            if (studentBatch.size() == batchSize) {
                jdbcTemplate.batchUpdate(sql, studentBatch);
                studentBatch.clear();
            }
        }
        if (!studentBatch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, studentBatch);
        }

        long elapsed = System.currentTimeMillis() - start;
        return message("[배치 INSERT] classroom " + classroomCount + "개 + student " + SAMPLE_SIZE + "건 생성 완료 (" + elapsed + "ms)");
    }

    /**
     * [GET] /api/index/v1/1
     * 1. 인덱스 없이 name 조회 - full scan 확인
     *    + 실제 쿼리 BENCH_RUNS회 수행 평균 시간 (full scan 이라 100만 건 기준 상당히 느림)
     */
    @GetMapping("/1")
    public Map<String, Object> step1() {
        return explainAndBench("1. 인덱스 없이 name 조회",
                "SELECT * FROM student WHERE name = ?", SAMPLE_NAME);
    }

    /**
     * [GET] /api/index/v1/2/index
     * 2. idx_student_name 인덱스 생성 (100만 건 대상이라 생성에 몇 초 걸림)
     */
    @GetMapping("/2/index")
    public Map<String, Object> step2Create() {
        jdbcTemplate.execute("CREATE INDEX idx_student_name ON student(name)");
        return message("2. idx_student_name 인덱스 생성 완료");
    }

    /**
     * [GET] /api/index/v1/2
     * 2. idx_student_name 생성 후 동일 쿼리 재실행 - type/key/rows 변화 확인
     *    + 실제 쿼리 BENCH_RUNS회 수행 평균 시간 (1번과 비교하면 수백~수천 배 차이)
     */
    @GetMapping("/2")
    public Map<String, Object> step2() {
        return explainAndBench("2. idx_student_name 생성 후 동일 쿼리 재실행",
                "SELECT * FROM student WHERE name = ?", SAMPLE_NAME);
    }

    /**
     * [GET] /api/index/v1/3-1

     * 3-1. LIKE '%500000%' - 앞 와일드카드, 인덱스 못 탐
     *      + 실제 쿼리 BENCH_RUNS회 수행 평균 시간 (full scan 반복, 오래 걸림 주의)
     */
    @GetMapping("/3-1")
    public Map<String, Object> step4a() {
        return explainAndBench("3-1. LIKE '%500000%' - 앞 와일드카드, 인덱스 못 탐",
                "SELECT * FROM student WHERE name LIKE ?", "%500000%");
    }

    /**
     * [GET] /api/index/v1/3-2
     * 3-2. LIKE '학생50000%' - 뒤 와일드카드 + 높은 선택도
     *      -> type=range, key=idx_student_name 으로 인덱스 range 스캔 확인
     *      + 실제 쿼리 수행 평균 시간 (4-1 full scan 과 극적인 차이)
     */
    @GetMapping("/3-2")
    public Map<String, Object> step4b() {
        return explainAndBench("3-2. LIKE '학생50000%' - 뒤 와일드카드 + 높은 선택도, 인덱스 range 스캔",
                "SELECT * FROM student WHERE name LIKE ?", "학생50000%");
    }

    /**
     * [GET] /api/index/v1/3-3
     * 3-3. LIKE '학생5%' - 뒤 와일드카드지만 낮은 선택도(약 1.2만 건, 전체의 12%)
     *      -> 인덱스를 "탈 수 있는" 형태인데도 옵티마이저가 type=ALL(full scan)을 선택.
     *      이유: SELECT * 라서 매칭된 1.2만 건마다 세컨더리 인덱스 -> 테이블로
     *      되돌아가는 랜덤 I/O가 필요한데, 그 비용이 테이블 순차 스캔 1회보다 비싸다고 판단.
     *      핵심 메시지: "인덱스를 탈 수 있다 != 타는 게 이득이다" (비용 기반 옵티마이저)
     *      4-2와 나란히 비교: 와일드카드 위치는 같고, 선택도만 다르다.
     */
    @GetMapping("/3-3")
    public Map<String, Object> step4c() {
        return explainAndBench("3-3. LIKE '학생5%' - 낮은 선택도, 인덱스를 버리고 full scan 선택",
                "SELECT * FROM student WHERE name LIKE ?", "학생5%");
    }

    /**
     * [GET] /api/index/v1/3-4
     * 3. 커버링 인덱스 - 인덱스 컬럼만 SELECT하면 테이블 접근 없이 처리 (Extra: Using index)
     *    + 실제 쿼리 BENCH_RUNS회 수행 평균 시간 (SELECT * 인 2번과 비교)
     */
    @GetMapping("/3-4")
    public Map<String, Object> step5() {
        return explainAndBench("3-4. 커버링 인덱스 - name만 SELECT",
                "SELECT name FROM student WHERE name = ?", SAMPLE_NAME);
    }

    /**
     * [GET] /api/index/v1/3/drop
     * 3. idx_student_name 인덱스 삭제 (초기화)
     */
    @GetMapping("/3/drop")
    public Map<String, Object> step2Drop() {
        jdbcTemplate.execute("DROP INDEX idx_student_name ON student");
        return message("3. idx_student_name 인덱스 삭제 완료 (초기화)");
    }

    /**
     * [GET] /api/index/v1/4/index
     * 4. idx_student_role_name(role, name) 복합 인덱스 생성
     */
    @GetMapping("/4/index")
    public Map<String, Object> step3Create() {
        jdbcTemplate.execute("CREATE INDEX idx_student_role_name ON student(role, name)");
        return message("4. idx_student_role_name(role, name) 복합 인덱스 생성 완료");
    }

    /**
     * [GET] /api/index/v1/4-1
     * 4-1. WHERE role = ? AND name = ? - 복합 인덱스를 왼쪽 접두부터 그대로 활용
     *      + 실제 쿼리 BENCH_RUNS회 수행 평균 시간
     */
    @GetMapping("/4-1")
    public Map<String, Object> step3a() {
        return explainAndBench("4-1. WHERE role = ? AND name = ? - 복합 인덱스 활용",
                "SELECT * FROM student WHERE role = ? AND name = ?", SAMPLE_ROLE, SAMPLE_NAME);
    }

    /**
     * [GET] /api/index/v1/4-2
     * 4-2. WHERE name = ? 단독 조회 - 왼쪽 접두 원칙 위반으로 복합 인덱스를 못 탐
     *      (idx_student_name 은 /2/drop 으로 지운 상태에서 실행해야 차이가 보임)
     *      + 실제 쿼리 BENCH_RUNS회 수행 평균 시간 (full scan 이라 4-1과 큰 차이)
     */
    @GetMapping("/4-2")
    public Map<String, Object> step3b() {
        return explainAndBench("4-2. WHERE name = ? 단독 조회 - 복합 인덱스(role, name)의 왼쪽 접두 위반",
                "SELECT * FROM student WHERE name = ?", SAMPLE_NAME);
    }

    /**
     * 5. FK 인덱스 데모 사전 준비 (MySQL 클라이언트에서 직접 실행)
     *    InnoDB 는 FK 제약이 있으면 인덱스 삭제를 거부(에러 1553)하므로 FK 부터 제거해야 한다.
     */
//    -- FK 제약 이름 확인
//    SELECT CONSTRAINT_NAME
//    FROM information_schema.KEY_COLUMN_USAGE
//    WHERE TABLE_SCHEMA = DATABASE()
//      AND TABLE_NAME = 'student'
//      AND COLUMN_NAME = 'classroom_id'
//      AND REFERENCED_TABLE_NAME IS NOT NULL;
//
//    -- FK 제거 -> 남은 인덱스 확인 -> 인덱스 삭제 (before 상태 만들기)
//    ALTER TABLE student DROP FOREIGN KEY FK1rs4md9whkjqy20v181d18kfy;
//
//    SHOW INDEX FROM student;
//    DROP INDEX idx_student_classroom_id ON student;
//    DROP INDEX FK1rs4md9whkjqy20v181d18kfy ON student;
//
//    -- 데모 종료 후 FK 복구
//    ALTER TABLE student
//      ADD CONSTRAINT fk_student_classroom
//      FOREIGN KEY (classroom_id) REFERENCES classroom(id);

    /**
     * [GET] /api/index/v1/6/index
     * 5. classroom_id FK 인덱스 생성
     */
    @GetMapping("/5/index")
    public Map<String, Object> step6Create() {
        jdbcTemplate.execute("CREATE INDEX idx_student_classroom_id ON student(classroom_id)");
        return message("5. classroom_id FK 인덱스 생성 완료");
    }

    /**
     * [GET] /api/index/v1/5
     * 5. classroom_id로 JOIN - FK 인덱스 유무에 따른 JOIN 성능 비교
     *    (사전 준비: 위 주석의 SQL로 FK 제약 + 인덱스를 제거한 상태에서 시작)
     *    + 실제 쿼리 BENCH_RUNS회 수행 평균 시간
     */
    @GetMapping("/5")
    public Map<String, Object> step6() {
        return explainAndBench("5. classroom_id로 JOIN - FK 인덱스 유무에 따른 성능 비교",
                "SELECT s.name, c.room_name FROM student s " +
                        "JOIN classroom c ON s.classroom_id = c.id WHERE c.id = ?", 1);
    }

}
