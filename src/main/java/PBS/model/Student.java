package PBS.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Student extends PanacheEntity {
    public String firstName;
    public String lastName;
//    public String department; //TODO multiple faculties/departments?
//    public String faculty;

}
