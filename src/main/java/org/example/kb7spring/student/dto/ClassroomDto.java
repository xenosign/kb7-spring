package org.example.kb7spring.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kb7spring.student.domain.Classroom;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassroomDto {
    private Long id;
    private String roomName;
    private List<StudentDto> students;

    public static ClassroomDto from(Classroom classroom) {
        return new ClassroomDto(
                classroom.getId(),
                classroom.getRoomName(),
                classroom.getStudents().stream()
                        .map(StudentDto::from)
                        .collect(Collectors.toList())
        );
    }
}