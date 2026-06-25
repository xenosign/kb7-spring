package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.dto.StudentDto;
import org.example.kb7spring.student.dto.StudentSearchDto;
import org.example.kb7spring.student.service.StudentServiceV2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@RequestMapping("/api/student/v2")
public class StudentApiControllerV2 {
    private final StudentServiceV2 studentService;

    @GetMapping("/list")
    public ResponseEntity<List<StudentDto>> list() {
        log.info("====================> StudentApiControllerV2 /list");

        List<StudentDto> list = studentService.getStudentList();

        return ResponseEntity.ok(list);
    }

    @GetMapping(value = "/test", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> test() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("요청을 처리할 수 없습니다");
    }


    @PostMapping("/search")
    public List<StudentDto> search(@RequestBody StudentSearchDto searchDto,
                         Model model) {
        log.info("====================> StudentApiControllerV1 /search, {}", searchDto);

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


//    @PostMapping("/add")
//    public void add(@RequestParam String name,
//                      @RequestParam String role,
//                      @RequestParam(required = false) String specialty,
//                      @RequestParam(required = false) String status) {
//        log.info("====================> StudentController POST /add : name={}, role={}", name, role);
//        StudentDto studentDto = new StudentDto();
//        studentDto.setName(name);
//        studentDto.setRole(role);
//        studentDto.setSpecialty(specialty);
//        studentDto.setStatus(status);
//        studentService.addStudent(studentDto);
//    }

    @PostMapping("/add")
    public void add2(@RequestBody StudentDto studentDto) {
        studentService.addStudent(studentDto);
    }
}
