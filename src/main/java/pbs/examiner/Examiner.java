package pbs.examiner;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;


import java.util.List;

@Entity
@Table(name = "examiners")
public class Examiner extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@pbs\\.edu\\.pl$")
    @Column(unique = true, nullable = false)
    public String mail;
    @Column(nullable = false)
    String password;
    @Column(nullable = false)
    public String firstName;
    @Column(nullable = false)
    public String lastName;
    @Column(nullable = false)
    public String titles;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns =
    @JoinColumn(name = "id"))
    public List<String> roles;

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return titles + " " + firstName + " " + lastName;
    }
}
