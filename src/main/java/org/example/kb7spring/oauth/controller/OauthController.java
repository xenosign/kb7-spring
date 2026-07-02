package org.example.kb7spring.oauth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.oauth.dto.KakaoUserInfo;
import org.example.kb7spring.oauth.service.KakaoOauthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/oauth")
public class OauthController {
    private final KakaoOauthService kakaoOauthService;
    private KakaoUserInfo kakaoUserInfo;

    @GetMapping("/kakao/callback")
    public void kakaoCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {
        log.info("kakaoCallback code={}, state={}", code, state);

        KakaoUserInfo userInfo = kakaoOauthService.processKakaoLogin(code);
        kakaoUserInfo = userInfo;

        Cookie cookie = new Cookie("jwt", userInfo.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1시간

        response.addCookie(cookie);

        String frontendRedirect = (state != null) ? state : "http://localhost:5173";
        response.sendRedirect(frontendRedirect);
    }

    @GetMapping("/user/me")
    public ResponseEntity<KakaoUserInfo> getKakaoUserInfo() {
        return ResponseEntity.ok(kakaoUserInfo);
    }
}