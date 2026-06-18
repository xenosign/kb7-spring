package org.example.kb7spring.student.repository;

import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudentRepositoryV1 implements StudentRepository {
    private final StudentMapper studentMapper;

    @Autowired
    public StudentRepositoryV1(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    public List<Student> findAll() {
        return studentMapper.findAll();
    }
}
