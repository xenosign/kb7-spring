package org.example.kb7spring.student.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.StudentSearchDto;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Primary
@Slf4j
@Repository
public class StudentRepositoryJpaV1 implements StudentRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Student> findAll() {
        String jpql = "SELECT t FROM Student t";
        return em.createQuery(jpql, Student.class).getResultList();
    }

    @Override
    public void save(Student student) {
        em.persist(student);
    }

    @Override
    public List<Student> search(StudentSearchDto searchDto) {
        StringBuilder jpql = new StringBuilder("SELECT s FROM Student s WHERE 1=1");

        if (searchDto.getName() != null && !searchDto.getName().isEmpty()) {
            jpql.append(" AND s.name LIKE :name");
        }
        if (searchDto.getRole() != null && !searchDto.getRole().isEmpty()) {
            jpql.append(" AND s.role = :role");
        }

        TypedQuery<Student> query = em.createQuery(jpql.toString(), Student.class);

        if (searchDto.getName() != null && !searchDto.getName().isEmpty()) {
            query.setParameter("name", "%" + searchDto.getName() + "%");
        }
        if (searchDto.getRole() != null && !searchDto.getRole().isEmpty()) {
            query.setParameter("role", searchDto.getRole());
        }

        return query.getResultList();
    }
}
