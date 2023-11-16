package PBS.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Examiner extends PanacheEntity {

    public String firstName;
    public String lastName;
    public String titles;

    @Override
    public String toString() {
        return titles + " " + firstName + " " + lastName;
    }
}
