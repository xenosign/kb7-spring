package org.example.kb7spring.student.repository;

import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.StudentSearchDto;

import java.util.List;

public interface StudentRepository {
    List<Student> findAll();
    List<Student> search(StudentSearchDto searchDto);
    Student findById(Long id);
    void save(Student student);
    void update(Student student);
    void delete(Long id);
}
