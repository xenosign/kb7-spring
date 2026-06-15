package org.example.kb7spring.member.repository;

import org.example.kb7spring.member.domain.Member;
import java.util.ArrayList;
import java.util.List;

public class MemberRepositoryV0 {
    private static MemberRepositoryV0 instance;

    private MemberRepositoryV0() {}

    public static MemberRepositoryV0 getInstance() {
        if (instance == null) {
            instance = new MemberRepositoryV0();
        }

        return instance;
    }

    public List<Member> getMemberList() {
        List<Member> memberList = new ArrayList<>();

        memberList.add(new Member(1L, "ronaldo@example.com", "1985. 02. 05","호날두", "플래티넘", 300000000L));
        memberList.add(new Member(2L, "sjk@example.com", "1985. 09. 19", "송중기", "골드", 3000000L));
        memberList.add(new Member(3L, "xenosign@example.com", "1985. 11. 18", "이효석", "아이언", 10L));

        return memberList;
    }
}
