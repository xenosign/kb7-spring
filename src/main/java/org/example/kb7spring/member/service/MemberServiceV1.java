package org.example.kb7spring.member.service;

import org.example.kb7spring.member.domain.Member;
import org.example.kb7spring.member.dto.MemberDto;
import org.example.kb7spring.member.repository.MemberRepositoryV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MemberServiceV1 {
    private final MemberRepositoryV1 memberRepository;

    @Autowired
    public MemberServiceV1(MemberRepositoryV1 memberRepository) {
        this.memberRepository = memberRepository;
    }

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

    public void addMember(String name, String email) {
        Member newMember = new Member();

        newMember.setName(name);
        newMember.setEmail(email);

        newMember.setGrade("아이언");
        newMember.setAsset(100L);

        memberRepository.save(newMember);
    }
}
