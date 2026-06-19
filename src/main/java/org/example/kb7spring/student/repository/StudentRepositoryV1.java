package org.example.kb7spring.student.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.StudentSearchDto;
import org.example.kb7spring.student.mapper.StudentMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StudentRepositoryV1 implements StudentRepository {
    private final StudentMapper studentMapper;

    public List<Student> findAll() {
        return studentMapper.findAll();
    }

    public List<Student> search(StudentSearchDto searchDto) {
        return studentMapper.search(searchDto);
    }

    public Student findById(Long id) {
        return studentMapper.findById(id);
    }

    public void save(Student student) {
        studentMapper.insert(student);
        log.info("저장 된 교육생 : {}", student);
    }

    public void update(Student student) {
        studentMapper.update(student);
    }

    public void delete(Long id) {
        studentMapper.delete(id);
    }

//    public List<Student> search(String name, String role) {
//        List<Student> list = new ArrayList<>();
//
//        StringBuilder sql = new StringBuilder("SELECT * FROM student WHERE 1=1");
//        List<Object> params = new ArrayList<>();
//
//        if (name != null && !name.isEmpty()) {
//            sql.append(" AND name LIKE ?");
//            params.add("%" + name + "%");
//        }
//
//        if (role != null && !role.isEmpty()) {
//            sql.append(" AND role = ?");
//            params.add(role);
//        }
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
//
//            for (int i = 0; i < params.size(); i++) {
//                pstmt.setObject(i + 1, params.get(i));
//            }
//
//            ResultSet rs = pstmt.executeQuery();
//            while (rs.next()) {
//                Student s = new Student();
//                s.setName(rs.getString("name"));
//                s.setRole(rs.getString("role"));
//                s.setSpecialty(rs.getString("specialty"));
//                s.setStatus(rs.getString("status"));
//                list.add(s);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return list;
//    }
}
