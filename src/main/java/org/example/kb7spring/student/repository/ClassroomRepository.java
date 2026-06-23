package org.example.kb7spring.student.repository;

import org.example.kb7spring.student.domain.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    @Query("select distinct c " +
            "from Classroom c " +
            " join fetch c.students ")
    List<Classroom> findAllFetchJoin();
}
