package pbs.student;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
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

    public Uni<List<Student>> addStudentsCSV(File fileData) {
        return Uni.createFrom().item(() -> {List<Student> students = new ArrayList<>();
        try (FileReader fileReader = new FileReader(fileData)) {
            try (CSVReader reader = new CSVReader(fileReader)) {
                students = reader.readAll().stream().map(row -> new Student(row[0],row[1], row[2])).toList();
            } catch (CsvException e) {
                LOG.error(e);
            }
        } catch (FileNotFoundException e) {
            LOG.error(e);
        } catch (IOException e) {
            LOG.error(e);
        }
        students.forEach(student -> LOG.trace(student.toString()));
        addStudents(students);
        return students;
    });
    }
}
