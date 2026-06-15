package org.example.kb7spring.member.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberEntity {
    private Long id;
    private String email;
    private String name;
    private String grade;
    private Long asset;
}
