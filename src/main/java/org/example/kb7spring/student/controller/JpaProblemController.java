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
        log.info("11111111111111111111");
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
        log.info("22222222222222222222");
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
        log.info("33333333333333333333");
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
        log.info("44444444444444444444");
        List<Student> students = studentRepository.findAllFetchJoin();
        for (Student student : students) {
            log.info("{} -> {}", student.getName(), student.getClassroom().getRoomName());
        }
        return "OK";
    }

    /**
     * 5. 영속성 컨텍스트(1차 캐시) 확인
     */
    @Transactional
    @GetMapping("/5")
    public String step5() {
        log.info("55555555555555555555");
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
     * 6. LazyInitializationException 확인
     */
//    "트랜잭션이 없으면 Session이 Repository 호출 직후 닫히기 때문에, 그 이후에 Lazy 객체를 건드리면 터진다"
    @GetMapping("/6")
    public String step6() {
        log.info("66666666666666666666");
        Student student = studentRepository.findById(1L).orElse(null);
        if (student == null) return "Student Not Found";
        log.info(student.getClassroom().getRoomName());
        return "OK";
    }

    /**
     * 7. OneToMany Lazy Loading
     */
    @Transactional
    @GetMapping("/7")
    public String step7() {
        log.info("77777777777777777777");
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
     * 8. OneToMany N+1 문제
     */
    @Transactional
    @GetMapping("/8")
    public String step8() {
        log.info("88888888888888888888");
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
     * 9. Fetch Join으로 OneToMany N+1 해결
     */
    @Transactional
    @GetMapping("/9")
    public String step9() {
        log.info("99999999999999999999");
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
     * 10. 양방향 객체 그래프 탐색 (굳이 굳이)
     */
    @Transactional
    @GetMapping("/10")
    public String step10() {
        log.info("10 10 10 10 10 10 10 10 10 10");
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
        log.info("11 11 11 11 11 11 11 11 11 11");
        Classroom classroom = classroomRepository.findById(1L).orElse(null);
        return classroom;
    }

    @Transactional
    @GetMapping("/11dto")
    public ClassroomDto step111() {
        log.info("11dto 11dto 11dto 11dto 11dto 11dto 11dto");
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
        log.info("12 12 12 12 12 12 12 12 12 12");
        Classroom classroom = classroomRepository.findById(1L).orElse(null);
        if (classroom == null) return "Classroom Not Found";

        // @Data가 생성한 toString()이 양방향 참조를 타고 무한순환
        // Classroom.toString() -> Student.toString() -> Classroom.toString() -> ...
        log.info(classroom.toString()); // 💥 StackOverflowError

        return "OK";
    }

    /**
     * 13. @Data 문제 - 의도치 않은 Dirty Checking
     */
    @Transactional
    @GetMapping("/13")
    public String step13() {
        log.info("13 13 13 13 13 13 13 13 13 13");
        Student student = studentRepository.findById(1L).orElse(null);
        if (student == null) return "Student Not Found";

        log.info("변경 전 : {}", student.getName());

        // setter가 열려있어서 트랜잭션 안에서 필드 변경 시
        // 별도 save() 없이 자동으로 UPDATE 쿼리 발생
        student.setName("이름바뀜");

        log.info("변경 후 : {}", student.getName());
        // save() 호출 없음에도 UPDATE 나감 💥

        return "OK";
    }

    /**
     * 14. 연관관계의 주인이 아닌 쪽에서만 관계 설정
     */
    // 주인이 아닌쪽에서 하면 null 이 들어가서 잘못된 데이터 저장
    @Transactional
    @GetMapping("/14")
    public String step14() {
        log.info("14 14 14 14 14 14 14 14 14 14");
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
// 주인 쪽에서 하면 올바른 classroom id 삽입
    @Transactional
    @GetMapping("/15")
    public String step15() {
        log.info("15 15 15 15 15 15 15 15 15 15");
        Classroom classroom = classroomRepository.findById(1L).orElse(null);
        if (classroom == null) return "Classroom Not Found";
        Student student = new Student();
        student.setName("이순신");
        student.setClassroom(classroom);
        studentRepository.save(student);
        return "OK";
    }
}