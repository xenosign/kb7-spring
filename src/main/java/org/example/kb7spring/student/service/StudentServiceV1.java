package org.example.kb7spring.student.service;

import lombok.RequiredArgsConstructor;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.StudentDto;
import org.example.kb7spring.student.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceV1 implements StudentService {
    private final StudentRepository studentRepository;

    public List<StudentDto> getStudentList() {
        List<Student> entityList = studentRepository.findAll();
        List<StudentDto> dtoList = new ArrayList<>();

        for (Student entity : entityList) {
            StudentDto dto = new StudentDto();
            dto.setName(entity.getName());
            dto.setSpecialty(entity.getSpecialty());
            dto.setStatus(entity.getStatus());
            dto.setRole(entity.getRole());
            dtoList.add(dto);
        }

        return dtoList;
    }
}
