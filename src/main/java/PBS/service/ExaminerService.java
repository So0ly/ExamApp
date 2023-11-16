package PBS.service;

import PBS.model.Examiner;

import java.util.List;
import java.util.Optional;

public interface ExaminerService {
    List<Examiner> getExaminers();
    Optional<Examiner> getExaminerById(Long id);
    boolean deleteExaminer(Long id);
    void addExaminer(Examiner examiner);
    Examiner modifyExaminer(Long id, Examiner examiner);

}
