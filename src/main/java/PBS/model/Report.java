package PBS.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@NoArgsConstructor
public class Report extends PanacheEntity {
    public String studentID;
    public String  firstName;
    public String lastName; //TODO USOS database
    @ElementCollection
    @CollectionTable(name = "question", joinColumns = @JoinColumn(name = "reportId"))
    @MapKeyColumn(name = "question")
    @Column(name = "questionScore")
    public Map<String,Float> questions = new LinkedHashMap<>();
    public Float finalGrade;
    public LocalDateTime examDate;

    public Long examinerId;

    public Report(String studentID, String firstName, String lastName, Map<String, Float> questions, Long examinerId) {
        this.studentID = studentID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.questions = questions;
        this.examinerId = examinerId;
        this.examDate = LocalDateTime.now();
        double avg = questions.values().stream().
                mapToDouble(Float::doubleValue).average()
                .orElseThrow(() -> new RuntimeException("No grades in the question map"));
        this.finalGrade = Math.round(avg/0.5F) * 0.5F;
    }
}
