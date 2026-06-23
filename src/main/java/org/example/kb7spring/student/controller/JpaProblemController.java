package org.example.kb7spring.student.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.domain.Classroom;
import org.example.kb7spring.student.domain.Student;
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
        Student student = studentRepository.findById(1L).orElse(null);

        if (student == null) {
            return "Student Not Found";
        }

        log.info("학생 이름 : {}", student.getName());

        return "OK";
    }

    /**
     * 2. 객체 그래프 탐색 + Lazy Loading
     */
    @Transactional
    @GetMapping("/2")
    public String step2() {
        Student student = studentRepository.findById(1L).orElse(null);

        if (student == null) {
            return "Student Not Found";
        }

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
        Student student = studentRepository.findById(1L).orElse(null);

        if (student == null) {
            return "Student Not Found";
        }

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
    @GetMapping("/6")
    public String step6() {
        Student student = studentRepository.findById(1L).orElse(null);

        if (student == null) {
            return "Student Not Found";
        }

        // Transaction 없이 Lazy 객체 접근
        log.info(student.getClassroom().getRoomName());

        return "OK";
    }

    /**
     * 7. OneToMany Lazy Loading
     */
    @Transactional
    @GetMapping("/7")
    public String step7() {
        Classroom classroom = classroomRepository.findById(1L).orElse(null);

        if (classroom == null) {
            return "Classroom Not Found";
        }

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
     * 10. 양방향 객체 그래프 탐색
     */
    @Transactional
    @GetMapping("/10")
    public String step10() {
        Student student = studentRepository.findById(1L).orElse(null);

        if (student == null) {
            return "Student Not Found";
        }

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
        Classroom classroom = classroomRepository.findById(1L).orElse(null);

        if (classroom == null) {
            return null;
        }

        return classroom;
    }

    /**
     * 12. 연관관계의 주인이 아닌 쪽에서만 관계 설정
     */
    @Transactional
    @GetMapping("/12")
    public String step12() {
        Classroom classroom = classroomRepository.findById(1L).orElse(null);

        if (classroom == null) {
            return "Classroom Not Found";
        }

        Student student = new Student();
        student.setName("홍길동");

        // 연관관계의 주인이 아님
        classroom.getStudents().add(student);

        studentRepository.save(student);

        return "OK";
    }

    /**
     * 13. 연관관계의 주인(ManyToOne)에서 관계 설정
     */
    @Transactional
    @GetMapping("/13")
    public String step13() {
        Classroom classroom = classroomRepository.findById(1L).orElse(null);

        if (classroom == null) {
            return "Classroom Not Found";
        }

        Student student = new Student();
        student.setName("이순신");

        // 연관관계의 주인에서 관계 설정
        student.setClassroom(classroom);

        studentRepository.save(student);

        return "OK";
    }
}