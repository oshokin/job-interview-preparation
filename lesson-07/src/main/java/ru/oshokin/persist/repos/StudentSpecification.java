package ru.oshokin.persist.repos;

import org.springframework.data.jpa.domain.Specification;
import ru.oshokin.persist.entities.Student;

import java.math.BigDecimal;

public class StudentSpecification {

    public static Specification<Student> nameLike(String name) {
        return (root, query, builder) -> builder.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<Student> minAge(Short minAge) {
        return (root, query, builder) -> builder.ge(root.get("age"), minAge);
    }

    public static Specification<Student> maxAge(Short maxAge) {
        return (root, query, builder) -> builder.le(root.get("age"), maxAge);
    }

}
