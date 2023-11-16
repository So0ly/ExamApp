package PBS.service;

import PBS.model.Student;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StudentServiceImpl implements StudentService{

    private final static Logger LOG = Logger.getLogger(StudentServiceImpl.class);
    @Override
    public List<Student> getStudents() {
        return Student.listAll();
    }

    @Override
    public Optional<Student> getStudentById(Long id) {
        return Student.findByIdOptional(id);
    }

    @Override
    @Transactional
    public boolean deleteStudent(Long id) {
        return Student.deleteById(id);
    }

    @Override
    @Transactional
    public void addStudent(Student student) {
        Student.persist(student);
    }

    @Override
    @Transactional
    public Student modifyStudent(Long id, Student newData) {
        Optional<Student> studentOptional = Student.findByIdOptional(id);
        if (studentOptional.isEmpty()){
            Student.persist(newData);
            return newData;
        }
        Student student = studentOptional.get();
        student.firstName = newData.firstName;
        student.lastName = newData.lastName;
        student.persist();
        return student;
    }

    public List<Student> addStudentCSV(FormData fileData){
        List<Student> students = new ArrayList<>(); //TODO send and decode csv data
//        try (InputStream fileStream = fileData.)
//        try (CSVReader reader = new CSVReader(new (data))){
//            students = reader.readAll().stream().map(row -> new Student(row[0], row[1])).toList();
//        }catch (IOException e){
//            LOG.trace(e.getStackTrace());
//            System.out.println(e.getMessage());
//        } catch (CsvException e) {
//            LOG.trace(e.getStackTrace());
//            System.out.println(e.getMessage());
//        }
        return students;
    }
}
