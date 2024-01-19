package pbs.report;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class ReportQuestions {
    private String question;
    private Float grade;
}
