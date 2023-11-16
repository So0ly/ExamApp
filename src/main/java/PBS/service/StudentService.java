package PBS.service;

import PBS.model.Student;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;

import java.util.List;
import java.util.Optional;

public interface StudentService {
    List<Student> getStudents();
    Optional<Student> getStudentById(Long id);
    boolean deleteStudent(Long id);
    void addStudent(Student student);
    Student modifyStudent(Long id, Student student);
    List<Student> addStudentCSV(FormData fileData);

}
