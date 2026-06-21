package org.example.kb7spring.student.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.StudentSearchDto;
import org.example.kb7spring.student.mapper.StudentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StudentRepositoryV2 implements JpaRe  {
    private final StudentMapper studentMapper;

    public List<Student> findAll() {
        return studentMapper.findAll();
    }

    public List<Student> search(StudentSearchDto searchDto) {
        return studentMapper.search(searchDto);
    }

    public void save(Student student) {
        studentMapper.insert(student);
        log.info("저장 된 교육생 : {}", student);
    }
}
