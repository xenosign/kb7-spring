package org.example.kb7spring.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.StudentSearchDto;

import java.util.List;

@Mapper
public interface StudentMapper {
    List<Student> findAll();
    void insert(Student student);
    List<Student> search(StudentSearchDto searchDto);


    Student findById(Long id);
    void update(Student student);
    void delete(Long id);
}
