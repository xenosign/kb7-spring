package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.dto.StudentDto;
import org.example.kb7spring.student.dto.StudentSearchDto;
import org.example.kb7spring.student.service.StudentServiceV2;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/student/v1")
public class StudentApiControllerV1 {
    private final StudentServiceV2 studentService;

    @GetMapping("/list")
    public List<StudentDto> list() {
        log.info("====================> StudentApiControllerV1 /list");

        return studentService.getStudentList();
    }

    @GetMapping("/search")
    public List<StudentDto> search(@ModelAttribute StudentSearchDto searchDto,
                         Model model) {
        log.info("====================> StudentApiControllerV1 /search, {}", searchDto);

        return studentService.searchStudentList(searchDto);
    }


    @PostMapping("/add")
    public void add(@RequestParam String name,
                      @RequestParam String role,
                      @RequestParam(required = false) String specialty,
                      @RequestParam(required = false) String status) {
        log.info("====================> StudentController POST /add : name={}, role={}", name, role);
        StudentDto studentDto = new StudentDto();
        studentDto.setName(name);
        studentDto.setRole(role);
        studentDto.setSpecialty(specialty);
        studentDto.setStatus(status);
        studentService.addStudent(studentDto);
    }
}
