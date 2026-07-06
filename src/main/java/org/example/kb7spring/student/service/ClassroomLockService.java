package org.example.kb7spring.student.service;

import lombok.RequiredArgsConstructor;
import org.example.kb7spring.student.domain.Classroom;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.repository.ClassroomRepository;
import org.example.kb7spring.student.repository.StudentRepositoryV2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// StudentServiceV4 안에서 this::로 호출하면 프록시를 안 타서 @Transactional이 무시되므로
// (self-invocation 문제) 낙관/비관 락이 필요한 메서드를 별도 빈으로 분리했다.
@Service
@RequiredArgsConstructor
public class ClassroomLockService {
    private final ClassroomRepository classroomRepository;
    private final StudentRepositoryV2 studentRepository;

    // 비관적 락 - Classroom row 를 FOR UPDATE 로 잠근 뒤 확인+등록을 한 트랜잭션 안에서 처리
    @Transactional
    public boolean enrollPessimistic(Long classroomId) {
        Classroom classroom = classroomRepository.findByIdForUpdate(classroomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반입니다: " + classroomId));

        long currentCount = studentRepository.countByClassroomId(classroomId);
        if (currentCount >= classroom.getCapacity()) {
            return false;
        }

        Student student = new Student();
        student.setName("신청자-" + System.nanoTime());
        student.setClassroom(classroom);
        studentRepository.save(student);
        return true;
    }

    // 낙관적 락 - currentEnrollment 를 증가시키고 flush 시점에 @Version 충돌 여부를 확인
    // 충돌 시 ObjectOptimisticLockingFailureException 이 던져지며, 재시도는 호출자(StudentServiceV4) 책임
    @Transactional
    public boolean enrollOptimisticOnce(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반입니다: " + classroomId));

        if (classroom.getStudentCount() >= classroom.getCapacity()) {
            return false;
        }

        classroom.setStudentCount(classroom.getStudentCount() + 1);
        classroomRepository.saveAndFlush(classroom);

        Student student = new Student();
        student.setName("신청자-" + System.nanoTime());
        student.setClassroom(classroom);
        studentRepository.save(student);
        return true;
    }
}
