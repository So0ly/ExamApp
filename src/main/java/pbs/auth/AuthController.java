package pbs.auth;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.annotations.Pos;

@Path("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PermitAll
    @POST
    @Path("/login")
    public Uni<String> login(AuthRequest request){
        return authService.authenticate(request);
    }
}
