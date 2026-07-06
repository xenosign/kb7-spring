package org.example.kb7spring.student.repository;

import org.example.kb7spring.student.domain.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    @Query("select distinct c " +
            "from Classroom c " +
            " join fetch c.students ")
    List<Classroom> findAllFetchJoin();

    // 비관적 락 - SELECT ... FOR UPDATE 로 다른 트랜잭션의 접근을 막음
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Classroom c where c.id = :id")
    Optional<Classroom> findByIdForUpdate(@Param("id") Long id);
}
