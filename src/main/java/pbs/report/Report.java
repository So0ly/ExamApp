package pbs.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import pbs.examiner.Examiner;
import pbs.student.Student;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@NoArgsConstructor
@Table(name = "reports")
public class Report extends PanacheEntity {
    @ManyToOne(optional = false)
    public Student student;
    public String className;
    @ElementCollection
    @CollectionTable(name = "questions", joinColumns = @JoinColumn(name = "reportId"))
    @MapKeyColumn(name = "question")
    @Column(name = "questionScore")
    @Fetch(FetchMode.JOIN)
    public Map<String,Float> questions = new LinkedHashMap<>();
    public Float finalGrade;
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public LocalDateTime examDate;

    @ManyToOne(optional = false)
    public Examiner examiner;

    public void setFinalGrade() {
        double avg = this.questions.values().stream().
                mapToDouble(Float::doubleValue).average()
                .orElseThrow(() -> new RuntimeException("No grades in the question map"));
        this.finalGrade = Math.round(avg/0.5F) * 0.5F;
    }
}
