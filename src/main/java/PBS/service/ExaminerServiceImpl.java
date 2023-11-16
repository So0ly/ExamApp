package PBS.service;

import PBS.model.Examiner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ExaminerServiceImpl implements ExaminerService{
    @Override
    public List<Examiner> getExaminers() {
        return Examiner.listAll();
    }

    @Override
    public Optional<Examiner> getExaminerById(Long id) {
        return Examiner.findByIdOptional(id);
    }

    @Override
    @Transactional
    public boolean deleteExaminer(Long id) {
        return Examiner.deleteById(id);
    }

    @Override
    @Transactional
    public void addExaminer(Examiner examiner) {
        Examiner.persist(examiner);
    }

    @Override
    @Transactional
    public Examiner modifyExaminer(Long id, Examiner newData) {
        Optional<Examiner> examiner = Examiner.findByIdOptional(id);
        return null;
    }
}
