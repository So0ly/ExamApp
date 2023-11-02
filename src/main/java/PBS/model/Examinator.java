package PBS.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Examinator extends PanacheEntity {
    public String firstName;
    public String lastName;
}
