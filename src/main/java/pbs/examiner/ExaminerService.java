package pbs.examiner;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

public interface ExaminerService {
    Uni<List<Examiner>> list();
    Uni<Examiner> findById(Long id);
    Uni<Examiner> findByMail(String mail);
    Uni<Void> delete(Long id);
    Uni<Examiner> add(Examiner examiner);
    Uni<Examiner> update(Examiner examiner);
    Uni<Examiner> getCurrentUser();
    Uni<Examiner> changePassword(String currentPassword, String newPassword);

    static boolean matches(Examiner examiner, String password) {
        return BcryptUtil.matches(password, examiner.password);
    }

}
