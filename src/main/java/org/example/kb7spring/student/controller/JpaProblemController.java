package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.domain.Classroom;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.ClassroomDto;
import org.example.kb7spring.student.repository.ClassroomRepository;
import org.example.kb7spring.student.repository.StudentRepositoryV2;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/jpa/v1")
public class JpaProblemController {

    private final StudentRepositoryV2 studentRepository;
    private final ClassroomRepository classroomRepository;

    /**
     * 1. Student만 조회
     */
    @GetMapping("/1")
    public String step1() {
        log.info("1. Student만 조회");
        Student student = studentRepository.findById(1L).orElse(null);
        if (student == null) return "Student Not Found";
        log.info("학생 이름 : {}", student.getName());
        return "OK";
    }

    /**
     * 2. 객체 그래프 탐색 + Lazy Loading
     */
    @Transactional
    @GetMapping("/2")
    public String step2() {
        log.info("2. 객체 그래프 탐색 + Lazy Loading");
        Student student = studentRepository.findById(1L).orElse(null);
        if (student == null) return "Student Not Found";
        log.info("학생 조회 완료");
        log.info("----------------");
        log.info("반 이름 : {}", student.getClassroom().getRoomName());
        return "OK";
    }

    /**
     * 3. N+1 문제
     */
    @Transactional
    @GetMapping("/3")
    public String step3() {
        log.info("3. N+1 문제");
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            log.info("{} -> {}", student.getName(), student.getClassroom().getRoomName());
        }
        return "OK";
    }

    /**
     * 4. Fetch Join으로 N+1 해결
     */
    @Transactional
    @GetMapping("/4")
    public String step4() {
        log.info("4. Fetch Join으로 N+1 해결");
        List<Student> students = studentRepository.findAllFetchJoin();
        for (Student student : students) {
            log.info("{} -> {}", student.getName(), student.getClassroom().getRoomName());
        }
        return "OK";
    }

    /**
     * 5. OneToMany Lazy Loading
     */
    @Transactional
    @GetMapping("/5")
    public String step5() {
        log.info("5. OneToMany Lazy Loading");
        Classroom classroom = classroomRepository.findById(1L).orElse(null);
        if (classroom == null) return "Classroom Not Found";
        log.info("반 조회 완료");
        log.info("----------------");
        for (Student student : classroom.getStudents()) {
            log.info(student.getName());
        }
        return "OK";
    }

    /**
     * 6. OneToMany N+1 문제
     */
    @Transactional
    @GetMapping("/6")
    public String step6() {
        log.info("6. OneToMany N+1 문제");
        List<Classroom> classrooms = classroomRepository.findAll();
        for (Classroom classroom : classrooms) {
            log.info("===== {} =====", classroom.getRoomName());
            for (Student student : classroom.getStudents()) {
                log.info(student.getName());
            }
        }
        return "OK";
    }

    /**
     * 7. Fetch Join으로 OneToMany N+1 해결
     */
    @Transactional
    @GetMapping("/7")
    public String step7() {
        log.info("7. Fetch Join으로 OneToMany N+1 해결");
        List<Classroom> classrooms = classroomRepository.findAllFetchJoin();
        for (Classroom classroom : classrooms) {
            log.info("===== {} =====", classroom.getRoomName());
            for (Student student : classroom.getStudents()) {
                log.info(student.getName());
            }
        }
        return "OK";
    }

    /**
     * 8. 영속성 컨텍스트(1차 캐시) 확인
     */
    @Transactional
    @GetMapping("/8")
    public String step8() {
        log.info("8. 영속성 컨텍스트(1차 캐시) 확인");
        Student student = studentRepository.findById(1L).orElse(null);
        if (student == null) return "Student Not Found";
        log.info("첫 번째 접근");
        log.info(student.getClassroom().getRoomName());
        log.info("----------------");
        log.info("두 번째 접근");
        log.info(student.getClassroom().getRoomName());
        return "OK";
    }

    /**
     * 9. 트랜잭션 밖에서 Lazy Loading - LazyInitializationException
     */
    @GetMapping("/9")
    public String step9() {
        log.info("9. 트랜잭션 밖에서 Lazy Loading - LazyInitializationException");
        Student student = studentRepository.findById(1L).orElse(null);
        if (student == null) return "Student Not Found";
        log.info("첫 번째 접근");
        log.info(student.getClassroom().getRoomName());
        log.info("----------------");
        log.info("두 번째 접근");
        log.info(student.getClassroom().getRoomName());
        return "OK";
    }

    /**
     * 10. 양방향 객체 그래프 탐색
     */
    @Transactional
    @GetMapping("/10")
    public String step10() {
        log.info("10. 양방향 객체 그래프 탐색");
        Student student = studentRepository.findById(1L).orElse(null);
        if (student == null) return "Student Not Found";
        Classroom classroom = student.getClassroom();
        log.info("반 이름 : {}", classroom.getRoomName());
        for (Student s : classroom.getStudents()) {
            log.info("{} {}", s.getId(), s.getName());
        }
        return "OK";
    }

    /**
     * 11. JSON 무한 순환 확인
     */
    @GetMapping("/11")
    public Classroom step11() {
        log.info("11. JSON 무한 순환 확인");
        Classroom classroom = classroomRepository.findById(1L).orElse(null);
        return classroom;
    }

    @Transactional
    @GetMapping("/11dto")
    public ClassroomDto step11dto() {
        log.info("11dto. DTO로 JSON 무한 순환 해결");
        Classroom classroom = classroomRepository.findById(1L).orElse(null);
        if (classroom == null) return null;
        return ClassroomDto.from(classroom);
    }

    /**
     * 12. @Data 문제 - toString() 무한순환 (StackOverflowError)
     */
    @Transactional
    @GetMapping("/12")
    public String step12() {
        log.info("12. @Data 문제 - toString() 무한순환 (StackOverflowError)");
        Classroom classroom = classroomRepository.findById(1L).orElse(null);
        if (classroom == null) return "Classroom Not Found";
        log.info(classroom.toString());
        return "OK";
    }

    /**
     * 13. @Data 문제 - 의도치 않은 Dirty Checking
     */
    @Transactional
    @GetMapping("/13")
    public String step13() {
        log.info("13. @Data 문제 - 의도치 않은 Dirty Checking");
        Student student = studentRepository.findById(1L).orElse(null);
        if (student == null) return "Student Not Found";
        log.info("변경 전 : {}", student.getName());
        student.setName("이름바뀜");
        log.info("변경 후 : {}", student.getName());
        return "OK";
    }

    /**
     * 14. 연관관계의 주인이 아닌 쪽에서만 관계 설정
     */
    @Transactional
    @GetMapping("/14")
    public String step14() {
        log.info("14. 연관관계의 주인이 아닌 쪽에서 관계 설정 - classroom_id null 저장");
        Classroom classroom = classroomRepository.findById(1L).orElse(null);
        if (classroom == null) return "Classroom Not Found";
        Student student = new Student();
        student.setName("홍길동");
        classroom.getStudents().add(student);
        studentRepository.save(student);
        return "OK";
    }

    /**
     * 15. 연관관계의 주인(ManyToOne)에서 관계 설정
     */
    @Transactional
    @GetMapping("/15")
    public String step15() {
        log.info("15. 연관관계의 주인(ManyToOne)에서 관계 설정 - classroom_id 정상 저장");
        Classroom classroom = classroomRepository.findById(1L).orElse(null);
        if (classroom == null) return "Classroom Not Found";
        Student student = new Student();
        student.setName("이순신");
        student.setClassroom(classroom);
        studentRepository.save(student);
        return "OK";
    }
}