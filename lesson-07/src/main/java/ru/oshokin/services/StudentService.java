package ru.oshokin.services;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import ru.oshokin.persist.entities.Student;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StudentService {

    Page<Student> applyFilter(Map<String, Object> parameters);
    List<Student> findAll(Specification<Student> spec);
    Optional<Student> findById(Long id);
    void save(Student student);
    void deleteById(Long id);

}