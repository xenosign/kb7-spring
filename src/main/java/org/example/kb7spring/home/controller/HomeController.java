package org.example.kb7spring.home.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.member.service.MemberServiceV0;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/")
public class HomeController {
    // HomeController 도 동일하게 MemberServiceV0.getInstance() 로 하나의 인스턴스를 공유하여 사용
    private final MemberServiceV0 memberServiceV0 = MemberServiceV0.getInstance();

    @GetMapping("")
    public String home(Model model) {
        log.info("====================> HomeController /");
        model.addAttribute("memberList", memberServiceV0.getMemberList());
        return "index";
    }
}
