package org.example.kb7spring.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kb7spring.student.domain.Student;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDto {
    private Long id;
    private String name;
    private String role;
    private String specialty;
    private String status;

    public static StudentDto from(Student student) {
        return new StudentDto(
                student.getId(),
                student.getName(),
                student.getRole(),
                student.getSpecialty(),
                student.getStatus()
        );
    }
}