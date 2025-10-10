package pbs.student;

import com.opencsv.bean.CsvBindByName;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "students")
public class Student extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false)
    @CsvBindByName(column = "imiona")
    public String firstName;
    @CsvBindByName(column = "nazwisko")
    public String lastName;
    @Pattern(regexp = "\\d{6}", message = "Index is a 6-digit number!")
    @Column(nullable = false, unique = true)
    @CsvBindByName(column = "nr albumu")
    public String index;
}
