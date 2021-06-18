package ru.oshokin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.oshokin.persist.entities.Student;
import ru.oshokin.persist.repos.StudentRepository;
import ru.oshokin.persist.repos.StudentSpecification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StudentServiceHandler implements StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentServiceHandler(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public Page<Student> applyFilter(Map<String, Object> parameters) {
        String[] filterFields = {"nameFilter", "minAge", "maxAge"};
        Specification<Student> spec = Specification.where(null);

        for (String fieldName : filterFields) {
            Object fieldValue = parameters.get(fieldName);
            if (fieldValue == null) continue;
            if (fieldName.equals("nameFilter")) {
                String castedValue = (String) fieldValue;
                if (!castedValue.isEmpty()) spec = spec.and(StudentSpecification.nameLike(castedValue));
            }
            if (fieldName.equals("minAge")) {
                spec = spec.and(StudentSpecification.minAge((Short) fieldValue));
            }
            if (fieldName.equals("maxAge")) {
                spec = spec.and(StudentSpecification.maxAge((Short) fieldValue));
            }
        }

        String sortField = (String) parameters.get("sortField");
        String sortOrder = (String) parameters.get("sortOrder");
        int page = (int) parameters.get("page");
        int size = (int) parameters.get("size");

        return studentRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.fromString(sortOrder), sortField)));
    }

    @Override
    public List<Student> findAll(Specification<Student> spec) {
        return studentRepository.findAll(spec);
    }

    @Override
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    @Transactional
    @Override
    public void save(Student student) {
        studentRepository.save(student);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

}