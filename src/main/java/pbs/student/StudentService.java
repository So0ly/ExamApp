package pbs.student;

import io.smallrye.mutiny.Uni;

import java.io.File;
import java.util.List;

public interface StudentService {
    Uni<List<Student>> getStudents();
    Uni<Student> getStudentById(Long id);
    Uni<Student> getStudentByIndex(String  index);
    Uni<Void> deleteStudent(Long id);
    Uni<Student> addStudent(Student student);
    Uni<Void> addStudents(List<Student> students);
    Uni<Student> update(Student student);
    Uni<List<Student>> addStudentsCSV(File fileData);

}
