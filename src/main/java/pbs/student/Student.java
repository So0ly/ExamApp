package pbs.student;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "students")
public class Student extends PanacheEntity {
    @Column(nullable = false)
    public String firstName;
    public String lastName;
    @Pattern(regexp = "\\d{6}", message = "Index is a 6-digit number!")
    @Column(nullable = false, unique = true)
    public String index;

    public Student(String firstName, String lastName, String index) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.index = index;
    }
}
