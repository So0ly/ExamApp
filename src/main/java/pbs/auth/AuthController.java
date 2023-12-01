package pbs.auth;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import pbs.examiner.Examiner;

@Path("/auth")
@PermitAll
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @POST
    @Path("/login")
    @Consumes("application/json")
    public Uni<String> login(AuthRequest request) {
        return authService.authenticate(request);
    }

    @POST
    @Path("/register")
    @Consumes("application/json")
    public Uni<Void> register(RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }
}
