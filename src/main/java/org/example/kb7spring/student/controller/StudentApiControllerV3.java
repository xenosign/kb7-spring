package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.dto.PageResponseDto;
import org.example.kb7spring.student.dto.StudentDto;
import org.example.kb7spring.student.dto.StudentSearchDto;
import org.example.kb7spring.student.service.StudentServiceV3;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@RequestMapping("/api/student/v3")
public class StudentApiControllerV3 {
    private final StudentServiceV3 studentService;

    // 전체 조회 - 페이지네이션/캐싱 없음 (비교 기준)
    @GetMapping("/all")
    public ResponseEntity<List<StudentDto>> all() {
        log.info("====================> StudentApiControllerV3 /all");

        List<StudentDto> result = studentService.getStudentList();

        return ResponseEntity.ok(result);
    }

    // 페이지네이션만 적용 - 캐싱 없음
    @GetMapping("/list")
    public ResponseEntity<PageResponseDto<StudentDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("====================> StudentApiControllerV3 /list, page={}, size={}", page, size);

        PageResponseDto<StudentDto> result = studentService.getStudentListPaged(page, size);

        return ResponseEntity.ok(result);
    }

    // 페이지네이션 + 1페이지 Redis 캐싱
    @GetMapping("/redis")
    public ResponseEntity<PageResponseDto<StudentDto>> redis(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        log.info("====================> StudentApiControllerV3 /redis, page={}, size={}", page, size);

        PageResponseDto<StudentDto> result = studentService.getStudentListCached(page, size);

        return ResponseEntity.ok(result);
    }

    // 어노테이션 기반 캐싱 처리
    @GetMapping("/redis2")
    public ResponseEntity<PageResponseDto<StudentDto>> redis2(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        log.info("====================> StudentApiControllerV3 /redis, page={}, size={}", page, size);

        PageResponseDto<StudentDto> result = studentService.getStudentListCached2(page, size);

        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/test", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> test() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("요청을 처리할 수 없습니다");
    }


    @PostMapping("/search")
    public List<StudentDto> search(@RequestBody StudentSearchDto searchDto,
                         Model model) {
        log.info("====================> StudentApiControllerV3 /search, {}", searchDto);

        return studentService.searchStudentList(searchDto);
    }

    @GetMapping({"/search2", "/search2/{name}", "/search2/{name}/{role}"})
    public List<StudentDto> search2(
            @PathVariable(required = false) String name,
            @PathVariable(required = false) String role) {

        StudentSearchDto searchDto = new StudentSearchDto();
        searchDto.setName(name);
        searchDto.setRole(role);
        return studentService.searchStudentList(searchDto);
    }

    @PostMapping("/add")
    public void add2(@RequestBody StudentDto studentDto) {
        studentService.addStudent(studentDto);
    }
}
