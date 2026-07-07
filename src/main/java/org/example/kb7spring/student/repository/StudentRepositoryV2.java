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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface StudentRepositoryV2 extends JpaRepository<Student, Long> {
    @Override
    List<Student> findAll();
    @Override
    <S extends Student> S save(S entity);

    List<Student> findByNameOrRole(String name, String role);

    long countByClassroomId(Long classroomId);

    @Transactional
    void deleteByClassroomId(Long classroomId);

    @Query("SELECT s FROM Student s " +
            "WHERE (:name IS NULL OR s.name = :name) " +
            "AND (:role IS NULL OR s.role = :role)")
    List<Student> search(@Param("name") String name, @Param("role") String role);

    @Query("select s " +
        "from Student s " +
        "join fetch s.classroom")
    List<Student> findAllFetchJoin();

    // 반 정합성 청크 배치용 - classroom 마다 개별 count 쿼리(N+1)를 날리는 대신
    // 전체 student 를 classroom_id 로 GROUP BY 해서 한 번의 쿼리로 집계한다.
    @Query("select s.classroom.id, count(s) " +
            "from Student s " +
            "group by s.classroom.id")
    List<Object[]> countGroupByClassroomId();
}
