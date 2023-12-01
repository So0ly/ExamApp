package pbs.auth;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.AuthenticationCompletionException;
import io.quarkus.security.AuthenticationFailedException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import pbs.examiner.Examiner;
import pbs.examiner.ExaminerService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class AuthServiceImpl implements AuthService{
    private static final Logger LOG = Logger.getLogger(AuthServiceImpl.class);
    private final String issuer;
    private final ExaminerService examinerService;

    public AuthServiceImpl(@ConfigProperty(name = "mp.jwt.verify.issuer") String issuer, ExaminerService examinerService){
        this.issuer = issuer;
        this.examinerService = examinerService;
    }

    @WithTransaction
    public Uni<String> authenticate(AuthRequest authRequest){
        LOG.trace(">>>authenticate");
        return examinerService.findByMail(authRequest.mail()).
                onItem().transform(examiner -> {
                    if (examiner == null || !ExaminerService.matches(examiner, authRequest.password())){
                        throw new AuthenticationFailedException("Invalid credentials");
                    }
                    return Jwt.issuer(issuer)
                            .upn(examiner.mail)
                            .groups(new HashSet<>(examiner.roles))
                            .expiresIn(Duration.ofHours(5L))
                            .sign();
                });
    }

    @WithTransaction
    public Uni<Void> register(RegisterRequest registerRequest) {
        LOG.trace(">>>register");
        return examinerService.findByMail(registerRequest.mail())
                .onItem().ifNotNull().failWith(() -> new AuthenticationCompletionException("Account with that email already exists"))
                .onItem().ifNull().continueWith(() -> {
                    Examiner newEx = new Examiner();
                    newEx.mail = registerRequest.mail();
                    newEx.firstName = registerRequest.firstName();
                    newEx.lastName = registerRequest.lastName();
                    newEx.titles = registerRequest.titles();
                    newEx.roles = List.of("user");
                    newEx.setPassword(registerRequest.password());
                    examinerService.add(newEx);
                    return newEx;
                })
                .replaceWith(Uni.createFrom().voidItem()); // Return Uni<Void>
    }

}
