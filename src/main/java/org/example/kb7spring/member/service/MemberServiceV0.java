package org.example.kb7spring.member.service;

import org.example.kb7spring.member.domain.Member;
import org.example.kb7spring.member.dto.MemberDto;
import org.example.kb7spring.member.repository.MemberRepositoryV0;

import java.util.ArrayList;
import java.util.List;

public class MemberServiceV0 {
    private static MemberServiceV0 instance;
    private final MemberRepositoryV0 memberRepository;

    public MemberServiceV0() {
        this.memberRepository = MemberRepositoryV0.getInstance();
    }

    public static MemberServiceV0 getInstance() {
        if (instance == null) {
            instance = new MemberServiceV0();
        }

        return instance;
    }

    public List<MemberDto> getMemberList() {
        List<Member> entityList = memberRepository.getMemberList();
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
