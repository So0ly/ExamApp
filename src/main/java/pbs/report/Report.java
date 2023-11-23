package pbs.report;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pbs.examiner.Examiner;
import pbs.student.Student;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@NoArgsConstructor
@Table(name = "reports")
public class Report extends PanacheEntity {
    @ManyToOne(optional = false)
    public Student student;
    @ElementCollection
    @CollectionTable(name = "questions", joinColumns = @JoinColumn(name = "reportId"))
    @MapKeyColumn(name = "question")
    @Column(name = "questionScore")
    public Map<String,Float> questions = new LinkedHashMap<>();
    public Float finalGrade;
    @CreationTimestamp
    public ZonedDateTime examDate;

    @ManyToOne(optional = false)
    public Examiner examiner;

    public void setFinalGrade() {
        double avg = this.questions.values().stream().
                mapToDouble(Float::doubleValue).average()
                .orElseThrow(() -> new RuntimeException("No grades in the question map"));
        this.finalGrade = Math.round(avg/0.5F) * 0.5F;
    }
}
