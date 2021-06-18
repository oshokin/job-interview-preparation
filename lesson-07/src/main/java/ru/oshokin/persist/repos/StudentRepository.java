package ru.oshokin.persist.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.oshokin.persist.entities.Student;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    List<Student> findStudentByNameLike(String name);
    List<Student> findByAgeBetween(BigDecimal min, BigDecimal max);

}