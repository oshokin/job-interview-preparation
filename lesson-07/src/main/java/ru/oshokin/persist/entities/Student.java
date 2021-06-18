package ru.oshokin.persist.entities;

import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = "students")
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @NotBlank(message = "Name shouldn''t be blank")
    @Column(length = 150, nullable = false)
    @Getter
    @Setter
    private String name;

    @NotNull(message = "Age shouldn''t be empty")
    @Column(nullable = false)
    @Range(min = 1, max = 150, message = "Too old to study")
    @Getter
    @Setter
    private Short age;

    public Student() {
    }

}