package org.example.kb7spring.member.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/member/v0")
public class MemberControllerV0 {
    @GetMapping("")
    public String home() {
        log.info("====================> MemberController V0 /");
        return "/member/index";
    }
}
