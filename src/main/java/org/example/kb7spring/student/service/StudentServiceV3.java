package org.example.kb7spring.student.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.student.domain.Student;
import org.example.kb7spring.student.dto.PageResponseDto;
import org.example.kb7spring.student.dto.StudentDto;
import org.example.kb7spring.student.dto.StudentSearchDto;
import org.example.kb7spring.student.repository.StudentRepositoryV2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentServiceV3 {
    private final StudentRepositoryV2 studentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FIRST_PAGE_CACHE_KEY = "student:list:firstPage";
    private static final long CACHE_TTL_SECONDS = 60;

    private static final String FIRST_PAGE_CACHE_NAME = "studentFirstPage";

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

    // 페이지네이션만 적용 - 캐시 없이 매번 DB 조회
    public PageResponseDto<StudentDto> getStudentListPaged(int page, int size) {
        return fetchPage(page, size);
    }

    // 페이지네이션 + 1페이지 Redis 캐싱
    @SuppressWarnings("unchecked")
    public PageResponseDto<StudentDto> getStudentListCached(int page, int size) {
        boolean isFirstPage = (page == 0);

        if (isFirstPage) {
            PageResponseDto<StudentDto> cached =
                    (PageResponseDto<StudentDto>) redisTemplate.opsForValue().get(FIRST_PAGE_CACHE_KEY);
            if (cached != null) {
                log.info("1페이지 캐시 HIT");
                return cached;
            }
            log.info("1페이지 캐시 MISS - DB 조회");
        }

        PageResponseDto<StudentDto> result = fetchPage(page, size);

        if (isFirstPage) {
            redisTemplate.opsForValue().set(FIRST_PAGE_CACHE_KEY, result, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        }

        return result;
    }

    // 페이지네이션 + 1페이지 Redis 캐싱 (어노테이션 기반)
    @Cacheable(value = FIRST_PAGE_CACHE_NAME, key = "'firstPage'", condition = "#page == 0")
    public PageResponseDto<StudentDto> getStudentListCached2(int page, int size) {
        log.info("1페이지 캐시 MISS - DB 조회");
        return fetchPage(page, size);
    }

    private PageResponseDto<StudentDto> fetchPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Student> studentPage = studentRepository.findAll(pageable);

        List<StudentDto> dtoList = new ArrayList<>();
        for (Student entity : studentPage.getContent()) {
            dtoList.add(StudentDto.from(entity));
        }

        return new PageResponseDto<>(
                dtoList,
                studentPage.getNumber(),
                studentPage.getSize(),
                studentPage.getTotalElements(),
                studentPage.getTotalPages()
        );
    }

    public void addStudent(StudentDto studentDto) {
        Student student = new Student();
        student.setName(studentDto.getName());
        student.setRole(studentDto.getRole());
        student.setSpecialty(studentDto.getSpecialty());
        student.setStatus(studentDto.getStatus());
        studentRepository.save(student);
        redisTemplate.delete(FIRST_PAGE_CACHE_KEY);
    }

    public List<StudentDto> searchStudentList(StudentSearchDto searchDto) {
        List<Student> entityList = studentRepository.findByNameOrRole(searchDto.getName(), searchDto.getRole());
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
}
