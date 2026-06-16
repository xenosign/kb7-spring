package org.example.kb7spring.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.member.service.MemberServiceV0;
import org.example.kb7spring.member.service.MemberServiceV1;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/member/v1")
public class MemberControllerV1 {
    private final MemberServiceV1 memberService;

    @GetMapping("")
    public String home() {
        log.info("====================> MemberController V1 /");
        return "/member/index";
    }

    @GetMapping("/list")
    public String list(Model model) {
        log.info("====================> MemberController V0 /list");
        model.addAttribute("memberList", memberService.getMemberList());
        return "/member/list";
    }
}
