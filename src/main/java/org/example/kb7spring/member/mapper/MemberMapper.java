package org.example.kb7spring.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.kb7spring.member.domain.Member;

import java.util.List;

@Mapper
public interface MemberMapper {
    List<Member> findAll();
}
