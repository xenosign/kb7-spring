package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 1회차 - DB 인덱스 처리 데모.
 * JpaProblemController 와 동일하게, 번호가 매겨진 엔드포인트를 순서대로 호출하며
 * EXPLAIN 결과 변화를 로그로 확인하는 방식.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/index/v1")
public class IndexDemoController {

    private final JdbcTemplate jdbcTemplate;

    private static final String SAMPLE_NAME = "학생50000";
    private static final String SAMPLE_ROLE = "수강생";

    private void explain(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("EXPLAIN " + sql, args);
        for (Map<String, Object> row : rows) {
            log.info("type={}, key={}, rows={}, Extra={}",
                    row.get("type"), row.get("key"), row.get("rows"), row.get("Extra"));
        }
    }

    /**
     * 0. 더미 데이터 대량 생성 (인덱스 효과를 보려면 최소 10만 건 필요)
     */
    @PostMapping("/seed")
    public String seed(@RequestParam(defaultValue = "100000") int count) {
        long start = System.currentTimeMillis();
        String sql = "INSERT INTO student (name, role, specialty, status) VALUES (?, ?, ?, ?)";
        String[] roles = {"수강생", "조교", "매니저"};
        String[] specialties = {"백엔드", "프론트엔드", "풀스택"};

        int batchSize = 1000;
        List<Object[]> batch = new ArrayList<>(batchSize);
        for (int i = 1; i <= count; i++) {
            batch.add(new Object[]{
                    "학생" + i,
                    roles[i % roles.length],
                    specialties[i % specialties.length],
                    "ACTIVE"
            });
            if (batch.size() == batchSize) {
                jdbcTemplate.batchUpdate(sql, batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("더미 데이터 {}건 생성 완료, {}ms 소요", count, elapsed);
        return count + "건 생성 완료 (" + elapsed + "ms)";
    }

    /**
     * 1. 인덱스 없이 name 조회 - full scan 확인
     */
    @GetMapping("/1")
    public String step1() {
        log.info("1. 인덱스 없이 name 조회");
        long start = System.currentTimeMillis();
        explain("SELECT * FROM student WHERE name = ?", SAMPLE_NAME);
        log.info("조회 소요 시간: {}ms", System.currentTimeMillis() - start);
        return "OK";
    }

    @PostMapping("/2/create")
    public String step2Create() {
        jdbcTemplate.execute("CREATE INDEX idx_student_name ON student(name)");
        log.info("2. idx_student_name 인덱스 생성 완료");
        return "OK";
    }

    /**
     * 2. idx_student_name 생성 후 동일 쿼리 재실행 - type/key/rows 변화 확인
     */
    @GetMapping("/2")
    public String step2() {
        log.info("2. idx_student_name 생성 후 동일 쿼리 재실행");
        long start = System.currentTimeMillis();
        explain("SELECT * FROM student WHERE name = ?", SAMPLE_NAME);
        log.info("조회 소요 시간: {}ms", System.currentTimeMillis() - start);
        return "OK";
    }

    @PostMapping("/4/create")
    public String step4Create() {
        jdbcTemplate.execute("CREATE INDEX idx_student_role_name ON student(role, name)");
        log.info("4. idx_student_role_name(role, name) 복합 인덱스 생성 완료");
        return "OK";
    }

    /**
     * 4a. WHERE role = ? AND name = ? - 복합 인덱스를 왼쪽 접두부터 그대로 활용
     */
    @GetMapping("/4a")
    public String step4a() {
        log.info("4a. WHERE role = ? AND name = ? - 복합 인덱스 활용");
        explain("SELECT * FROM student WHERE role = ? AND name = ?", SAMPLE_ROLE, SAMPLE_NAME);
        return "OK";
    }

    /**
     * 4b. WHERE name = ? 단독 조회 - 왼쪽 접두 원칙 위반으로 복합 인덱스를 못 탐
     *     (idx_student_name 은 2/drop 으로 지운 상태에서 실행해야 차이가 보임)
     */
    @GetMapping("/4b")
    public String step4b() {
        log.info("4b. WHERE name = ? 단독 조회 - 복합 인덱스(role, name)의 왼쪽 접두 위반");
        explain("SELECT * FROM student WHERE name = ?", SAMPLE_NAME);
        return "OK";
    }

    @PostMapping("/4/drop")
    public String step4Drop() {
        jdbcTemplate.execute("DROP INDEX idx_student_role_name ON student");
        log.info("4. idx_student_role_name 인덱스 삭제 완료 (초기화)");
        return "OK";
    }

    /**
     * 5. StudentRepositoryV2.search() 의 "(:name IS NULL OR s.name = :name)" 패턴을 그대로 재현.
     *    인덱스를 만들어도 옵티마이저가 활용하지 못하는 대표적인 안티패턴.
     */
    @GetMapping("/5")
    public String step5() {
        log.info("5. StudentRepositoryV2.search() 와 동일한 OR-NULL 패턴 - 인덱스를 만들어도 못 탐");
        explain("SELECT * FROM student WHERE (? IS NULL OR name = ?) AND (? IS NULL OR role = ?)",
                SAMPLE_NAME, SAMPLE_NAME, null, null);
        return "OK";
    }

    /**
     * 6. 5번 개선 - null 인 조건은 SQL에서 아예 빼고 자바 코드로 동적 구성
     */
    @GetMapping("/6")
    public String step6() {
        log.info("6. 5번 개선 - null 조건을 제거하고 동적 쿼리로 구성");
        String name = SAMPLE_NAME;
        String role = null;

        StringBuilder sql = new StringBuilder("SELECT * FROM student WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (name != null) {
            sql.append(" AND name = ?");
            params.add(name);
        }
        if (role != null) {
            sql.append(" AND role = ?");
            params.add(role);
        }
        explain(sql.toString(), params.toArray());
        return "OK";
    }

    /**
     * 7a. LIKE '%50000%' - 앞 와일드카드, 인덱스 못 탐
     */
    @GetMapping("/7a")
    public String step7a() {
        log.info("7a. LIKE '%50000%' - 앞 와일드카드, 인덱스 못 탐");
        explain("SELECT * FROM student WHERE name LIKE ?", "%50000%");
        return "OK";
    }

    /**
     * 7b. LIKE '학생5%' - 뒤 와일드카드, range 스캔으로 인덱스 활용 가능
     */
    @GetMapping("/7b")
    public String step7b() {
        log.info("7b. LIKE '학생5%' - 뒤 와일드카드, 인덱스 range 스캔 가능");
        explain("SELECT * FROM student WHERE name LIKE ?", "학생5%");
        return "OK";
    }

    /**
     * 8. 커버링 인덱스 - 인덱스 컬럼만 SELECT하면 테이블 접근 없이 처리 (Extra: Using index)
     */
    @GetMapping("/8")
    public String step8() {
        log.info("8. 커버링 인덱스 - name만 SELECT");
        explain("SELECT name FROM student WHERE name = ?", SAMPLE_NAME);
        return "OK";
    }

    @PostMapping("/9/create")
    public String step9Create() {
        jdbcTemplate.execute("CREATE INDEX idx_student_classroom_id ON student(classroom_id)");
        log.info("9. classroom_id FK 인덱스 생성 완료");
        return "OK";
    }

    /**
     * 9. classroom_id로 JOIN - FK 인덱스 유무에 따른 JOIN 성능 비교
     */
    @GetMapping("/9")
    public String step9() {
        log.info("9. classroom_id로 JOIN");
        explain("SELECT s.name, c.room_name FROM student s " +
                "JOIN classroom c ON s.classroom_id = c.id WHERE c.id = ?", 1L);
        return "OK";
    }

    @PostMapping("/9/drop")
    public String step9Drop() {
        jdbcTemplate.execute("DROP INDEX idx_student_classroom_id ON student");
        log.info("9. classroom_id FK 인덱스 삭제 완료 (초기화)");
        return "OK";
    }

    @PostMapping("/2/drop")
    public String step2Drop() {
        jdbcTemplate.execute("DROP INDEX idx_student_name ON student");
        log.info("2. idx_student_name 인덱스 삭제 완료 (초기화)");
        return "OK";
    }
}
