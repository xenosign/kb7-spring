package org.example.kb7spring.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KakaoUserInfo {
    private Long kakaoId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String token;
}