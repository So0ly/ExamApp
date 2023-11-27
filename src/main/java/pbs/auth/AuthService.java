package pbs.auth;

import io.smallrye.mutiny.Uni;

public interface AuthService {
    Uni<String> authenticate(AuthRequest authRequest);
}
