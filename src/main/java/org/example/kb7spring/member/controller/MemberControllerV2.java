package org.example.kb7spring.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.member.service.MemberService;
import org.example.kb7spring.member.service.MemberServiceV1;
import org.example.kb7spring.member.service.MemberServiceV2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/member/v2")
public class MemberControllerV2 {
    private final MemberService memberService;

    @GetMapping("")
    public String home() {
        log.info("====================> MemberController V2 /");
        return "/member/index";
    }

    @GetMapping("/list")
    public String list(Model model) {
        log.info("====================> MemberController V2 /list");
        model.addAttribute("memberList", memberService.getMemberList());
        return "/member/list";
    }
}
