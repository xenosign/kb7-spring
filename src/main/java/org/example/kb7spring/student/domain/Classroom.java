package org.example.kb7spring.student.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "classroom")
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(nullable = false, columnDefinition = "int default 30")
    private int capacity;

    // 낙관적 락(@Version) 충돌을 유발할 대상 - 등록 인원 카운터
    @Column(nullable = false, columnDefinition = "int default 0")
    private int studentCount;

    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long version;

    @JsonIgnore
    @OneToMany(mappedBy = "classroom")
    private List<Student> students = new ArrayList<>();
}
