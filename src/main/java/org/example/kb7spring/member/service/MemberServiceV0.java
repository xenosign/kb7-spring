package org.example.kb7spring.member.service;

import org.example.kb7spring.member.domain.Member;
import org.example.kb7spring.member.dto.MemberDto;
import org.example.kb7spring.member.repository.MemberRepositoryV0;

import java.util.ArrayList;
import java.util.List;

public class MemberServiceV0 {
    // 싱글톤으로 사용 될, 인스턴스의 주소를 저장하는 필드
    // Static 으로 서버 전체에서 사용
    private static MemberServiceV0 instance;
    private final MemberRepositoryV0 memberRepository = new MemberRepositoryV0();

    // 생성자를 private 를 만들어서 외부에서는 MemberServiceV0 를 인스턴스화 하여 사용 못하게 방지
    private MemberServiceV0() {}

    // MemberServiceV0 를 쓰고 싶으면 무조건 getInstance() 호출하여 최초 생성 된 하나의 인스턴스 참조만을 받아서 사용
    // 인스턴스가 생성이 안되어 있으면 생성하는 분기문 추가
    public static MemberServiceV0 getInstance() {
        if (instance == null) {
            instance = new MemberServiceV0();
        }

        return instance;
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
}

