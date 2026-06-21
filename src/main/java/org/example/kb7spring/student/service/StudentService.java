package org.example.kb7spring.student.service;

import org.example.kb7spring.student.dto.StudentDto;
import org.example.kb7spring.student.dto.StudentSearchDto;

import java.util.List;

public interface StudentService {
    List<StudentDto> getStudentList();
    List<StudentDto> searchStudentList(StudentSearchDto searchDto);
    void addStudent(StudentDto studentDto);
}
