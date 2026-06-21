package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.dto.StudentDto;
import org.example.kb7spring.student.dto.StudentSearchDto;
import org.example.kb7spring.student.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/student/v1")
public class StudentControllerV2 {
    private final StudentService studentService;

    @GetMapping("")
    public String home() {
        log.info("====================> StudentController /");
        return "/student/index";
    }

    @GetMapping("/list")
    public String list(Model model) {
        log.info("====================> StudentController /list");

        model.addAttribute("studentList", studentService.getStudentList());
        return "/student/list";
    }

    @GetMapping("/search")
    public String search(@ModelAttribute StudentSearchDto searchDto,
                       Model model) {
        log.info("====================> StudentController /list : searchDto", searchDto);

        model.addAttribute("studentList", studentService.searchStudentList(searchDto));
        return "/student/search";
    }

    @GetMapping("/add")
    public String addForm() {
        log.info("====================> StudentController GET /add");
        return "/student/add";
    }

    @PostMapping("/add")
    public String add(@RequestParam String name,
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
        return "redirect:/student/v1/list";
    }
}
