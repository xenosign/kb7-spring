package org.example.kb7spring.member.mapper;

import org.example.kb7spring.member.domain.Member;

import java.util.List;

public interface MemberMapper {
    // 전체 조회
    List<Member> getList();
    // 단건 조회
    Member get(Long no);
    // 등록
    int insert(Member member);
    // 수정
    int update(Member member);
    // 삭제
    int delete(Long no);
}
