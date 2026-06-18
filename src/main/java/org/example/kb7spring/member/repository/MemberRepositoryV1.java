package org.example.kb7spring.member.repository;

import org.example.kb7spring.member.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MemberRepositoryV1 implements MemberRepository {
    private final List<Member> memberList = new ArrayList<>();

    public MemberRepositoryV1() {
        memberList.add(new Member(1L, "ronaldo@example.com", "1985. 02. 05","호날두", "플래티넘", 300000000L));
        memberList.add(new Member(2L, "sjk@example.com", "1985. 09. 19", "송중기", "골드", 3000000L));
        memberList.add(new Member(3L, "xenosign@example.com", "1985. 11. 18", "이효석", "아이언", 10L));

    }

    public List<Member> findAll() {
        return memberList;
    }

    public void save(Member member) {
        member.setId((long) (memberList.size() + 1));
        memberList.add(member);
    }
}
