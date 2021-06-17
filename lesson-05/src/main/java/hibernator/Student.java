package hibernator;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "students")
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    @Getter
    @Setter
    private Integer Id;

    @Column(name = "name", nullable = false, length = 200)
    @Getter
    @Setter
    private String name;

    @Column(name = "mark", nullable = false)
    @Getter
    @Setter
    private Byte mark;

    public Student() {
    }

}