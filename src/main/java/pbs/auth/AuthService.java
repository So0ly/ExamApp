package pbs.auth;

import io.quarkus.security.AuthenticationFailedException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pbs.examiner.ExaminerService;

import java.time.Duration;
import java.util.HashSet;

@ApplicationScoped
public class AuthService {
    private final String issuer;
    private final ExaminerService examinerService;

    public AuthService(@ConfigProperty(name = "mp.jwt.verify.issuer") String issuer, ExaminerService examinerService){
        this.issuer = issuer;
        this.examinerService = examinerService;
    }

    public Uni<String> authenticate(AuthRequest authRequest){
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
}
