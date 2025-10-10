package pbs.student;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NoArgsConstructor;
import org.jboss.logging.Logger;
import pbs.model.CSVStudentBean;
import pbs.utils.CSVHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@NoArgsConstructor
public class StudentServiceImpl implements StudentService {

    private static final Logger LOG = Logger.getLogger(StudentServiceImpl.class);

    public Uni<List<Student>> getStudents() {
        return Student.listAll();
    }

    public Uni<Student> getStudentById(Long id) {
        return Student.findById(id);
    }

    public Uni<Student> getStudentByIndex(String index) {
        return Student.find("index", index).firstResult();
    }

    @WithTransaction
    public Uni<Void> deleteStudent(Long id) {
        return Student.delete("id", id).replaceWithVoid();
    }

    @WithTransaction
    public Uni<Student> addStudent(Student student) {
        return student.persistAndFlush();
    }

    @WithTransaction
    public Uni<Void> addStudents(List<Student> students) {
        return Student.persist(students);
    }

    @WithTransaction
    public Uni<Student> update(Student student) {
        return Student.findById(student.id)
                .chain(st -> Student.getSession())
                .chain(s -> s.merge(student));
    }

    public Uni<List<CSVStudentBean>> parseStudentsCSV(File fileData) {
        List<CSVStudentBean> students = CSVHelper.parseCSVIntoBeanList(fileData, CSVStudentBean.class);
        return Uni.createFrom().item(students);
    }
}
