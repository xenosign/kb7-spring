package org.example.kb7spring.student.repository;

import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.StudentSearchDto;

import java.util.List;

public interface StudentRepository {
    List<Student> findAll();
    void save(Student student);
    List<Student> search(StudentSearchDto studentSearchDto);
}
