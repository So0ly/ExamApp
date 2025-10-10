package pbs.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CSVStudentBean {
    @CsvBindByName(column = "imiona")
    private String firstName;
    @CsvBindByName(column = "nazwisko")
    private String lastName;
    @Pattern(regexp = "\\d{6}", message = "Index is a 6-digit number!")
    @CsvBindByName(column = "nr albumu", required = true)
    private String index;
}
