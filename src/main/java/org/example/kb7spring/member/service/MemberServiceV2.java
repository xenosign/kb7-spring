package org.example.kb7spring.member.service;

import lombok.RequiredArgsConstructor;
import org.example.kb7spring.member.domain.Member;
import org.example.kb7spring.member.dto.MemberDto;
import org.example.kb7spring.member.repository.MemberRepositoryV1;
import org.example.kb7spring.member.repository.MemberRepositoryV2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceV2 {
    private final MemberRepositoryV2 memberRepository;

    public List<MemberDto> getMemberList() {
        List<Member> entityList = memberRepository.findAll();
        List<MemberDto> dtoList = new ArrayList<>();

        for (Member entity : entityList) {
            MemberDto dto = new MemberDto();
            dto.setName(entity.getName());
            dto.setEmail(entity.getEmail());
            dtoList.add(dto);
        }

        return dtoList;
    }
}
