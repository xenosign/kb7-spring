package org.example.kb7spring.member.repository;

import org.example.kb7spring.member.domain.Member;
import org.example.kb7spring.member.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MemberRepositoryV2 {
    private final MemberMapper memberMapper;

    @Autowired
    public MemberRepositoryV2(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    public List<Member> findAll() {
        return memberMapper.findAll();
    }
}
