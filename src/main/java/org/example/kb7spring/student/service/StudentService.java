package org.example.kb7spring.student.service;

import org.example.kb7spring.student.dto.StudentDto;
import org.example.kb7spring.student.dto.StudentSearchDto;

import java.util.List;

public interface StudentService {
    List<StudentDto> getStudentList();
    List<StudentDto> searchStudentList(StudentSearchDto searchDto);
    StudentDto getStudent(Long id);
    void addStudent(StudentDto studentDto);
    void updateStudent(StudentDto studentDto);
    void deleteStudent(Long id);
}
