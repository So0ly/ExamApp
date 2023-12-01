package pbs.examiner;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.ObjectNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import pbs.report.Report;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ExaminerServiceImpl implements ExaminerService {

    private final JsonWebToken jwt;

    public Uni<List<Examiner>> list() {
        return Examiner.listAll();
    }

    public Uni<Examiner> findById(Long id) {
        return Examiner.<Examiner>findById(id)
                .onItem().ifNull().failWith(
                        () -> new ObjectNotFoundException(id, "Examiner"));
    }

    public Uni<Examiner> findByMail(String mail) {
        return Examiner.find("mail", mail).firstResult();
    }

    @WithTransaction
    public Uni<Void> delete(Long id) {
        return findById(id)
                .chain(ex -> Uni.createFrom().item(
                                        Report.delete("examiner.id", ex.id)
                                )
                                .chain(t -> ex.delete())
                );
    }

    @WithTransaction
    public Uni<Examiner> add(Examiner examiner) {
        examiner.password = BcryptUtil.bcryptHash(examiner.password);
        return examiner.persistAndFlush();
    }

    @WithTransaction
    public Uni<Examiner> update(Examiner examiner) {
        return Examiner.findById(examiner.id).chain(ex -> {
            Examiner castedEx = (Examiner) ex;
            examiner.setPassword(castedEx.password);
            return Examiner.getSession();
        }).chain(s -> s.merge(examiner));
    }

    public Uni<Examiner> getCurrentExaminer() {
        return findByMail(jwt.getName());
    }

    @WithTransaction
    public Uni<Examiner> changePassword(String currentPassword, String newPassword) {
        return getCurrentExaminer()
                .chain(ex -> {
                    if (!ExaminerService.matches(ex, currentPassword)) {
                        throw new ClientErrorException("Current password does not match", Response.Status.CONFLICT);
                    }
                    ex.setPassword(BcryptUtil.bcryptHash(newPassword));
                    return ex.persistAndFlush();
                });
    }
}
