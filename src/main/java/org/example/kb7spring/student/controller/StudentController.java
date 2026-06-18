package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/student/v1")
public class StudentController {
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
}
