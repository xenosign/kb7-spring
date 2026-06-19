package org.example.kb7spring.student.service;

import lombok.RequiredArgsConstructor;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.StudentDto;
import org.example.kb7spring.student.dto.StudentSearchDto;
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
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setSpecialty(entity.getSpecialty());
            dto.setStatus(entity.getStatus());
            dto.setRole(entity.getRole());
            dtoList.add(dto);
        }

        return dtoList;
    }

    public List<StudentDto> searchStudentList(StudentSearchDto searchDto) {
        List<Student> entityList = studentRepository.search(searchDto);
        List<StudentDto> dtoList = new ArrayList<>();

        for (Student entity : entityList) {
            StudentDto dto = new StudentDto();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setRole(entity.getRole());
            dto.setSpecialty(entity.getSpecialty());
            dto.setStatus(entity.getStatus());
            dtoList.add(dto);
        }

        return dtoList;
    }

    public StudentDto getStudent(Long id) {
        Student entity = studentRepository.findById(id);
        StudentDto dto = new StudentDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setRole(entity.getRole());
        dto.setSpecialty(entity.getSpecialty());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public void addStudent(StudentDto studentDto) {
        Student student = new Student();
        student.setName(studentDto.getName());
        student.setRole(studentDto.getRole());
        student.setSpecialty(studentDto.getSpecialty());
        student.setStatus(studentDto.getStatus());
        studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.delete(id);
    }

    public void updateStudent(StudentDto studentDto) {
        Student student = new Student();
        student.setId(studentDto.getId());
        student.setName(studentDto.getName());
        student.setRole(studentDto.getRole());
        student.setSpecialty(studentDto.getSpecialty());
        student.setStatus(studentDto.getStatus());
        studentRepository.update(student);
    }
}
