package org.example.kb7spring.member.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.member.dto.MemberDto;
import org.example.kb7spring.member.service.MemberServiceV0;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/member/v0")
public class MemberControllerV0 {
    private final MemberServiceV0 memberService = new MemberServiceV0();

    @GetMapping("")
    public String home() {
        log.info("====================> MemberController V0 /");
        return "/member/index";
    }

    @GetMapping("list")
    public String list(Model model) {
        log.info("====================> MemberController V0 /list");
        model.addAttribute("memberList", memberService.getMemberList());
        return "/member/list";
    }
}
