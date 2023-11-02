package PBS.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class Report extends PanacheEntity {
    public String studentID;
    public String  firstName;
    public String lastName; //TODO USOS database
    @ElementCollection
    @CollectionTable(name = "questions", joinColumns = @JoinColumn(name = "reportId"))
    @MapKeyColumn(name = "question")
    @Column(name = "questionScore")
    public Map<String,Float> questions = new LinkedHashMap<>();
    public Float finalGrade;
    public LocalDateTime examDate;
    public String examinator;

}
