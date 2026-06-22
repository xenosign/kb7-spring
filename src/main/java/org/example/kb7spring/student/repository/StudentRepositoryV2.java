package org.example.kb7spring.student.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.StudentSearchDto;
import org.example.kb7spring.student.mapper.StudentMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface StudentRepositoryV2 extends JpaRepository<Student, Long> {
    @Override
    List<Student> findAll();
    @Override
    <S extends Student> S save(S entity);

    List<Student> findByNameOrRole(String name, String role);

    @Query("SELECT s FROM Student s " +
            "WHERE (:name IS NULL OR s.name = :name) " +
            "AND (:role IS NULL OR s.role = :role)")
    List<Student> search(@Param("name") String name, @Param("role") String role);
}
