package org.example.kb7spring.member.repository;

import org.example.kb7spring.member.domain.Member;

import java.util.List;

public interface MemberRepository {
    public List<Member> findAll();
}
