package org.example.kb7spring.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.kb7spring.student.domain.Student;

import java.util.List;

@Mapper
public interface StudentMapper {
    List<Student> findAll();
}
