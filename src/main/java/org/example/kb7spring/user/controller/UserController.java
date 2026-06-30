package org.example.kb7spring.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.user.dto.UserJoinRequest;
import org.example.kb7spring.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/register")
    public String register() {
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute UserJoinRequest dto) {
        userService.save(dto);
        return "redirect:/user/login";
    }

    @GetMapping("/login-success")
    public String loginSuccess(Model model, Principal principal, Authentication authentication) {
        log.info("=========> User Principal : {}", principal);
        log.info("=========> User Auth : {}", authentication);

        model.addAttribute("user", principal.getName());
        model.addAttribute("auth", authentication.getAuthorities());

        return "user/login-success";
    }

    @GetMapping("/login-failure")
    public String loginFailed() {
        return "user/login-failure";
    }
}
