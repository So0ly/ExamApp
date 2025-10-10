package pbs.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class CSVQuestionBean {
    @CsvBindByName(column = "pytania", required = true)
    private String question;
    @CsvBindByName(column = "waga")
    private Integer weight;
}
