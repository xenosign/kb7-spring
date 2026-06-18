package org.example.kb7spring.student.repository;

import org.example.kb7spring.student.domain.Student;

import java.util.List;

public interface StudentRepository {
    List<Student> findAll();
}
