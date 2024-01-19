package pbs.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pbs.examiner.Examiner;
import pbs.student.Student;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "reports")
public class Report extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(optional = false)
    public Student student;
    public String className;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "questions", joinColumns =
    @JoinColumn(name = "reportId"))
    public List<ReportQuestions> reportQuestions;
    public Float finalGrade;
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public LocalDateTime examDate;

    public String examDuration;
    public String audioURL;

    @ManyToOne(optional = false)
    public Examiner examiner;

    public void setFinalGrade() {
        double avg = this.reportQuestions.stream().mapToDouble(ReportQuestions::getGrade).average()
                .orElseThrow(() -> new RuntimeException("No grades in the question map"));
        this.finalGrade = Math.round(avg/0.5F) * 0.5F;
    }
}
