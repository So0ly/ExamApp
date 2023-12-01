package pbs.auth;

public record RegisterRequest(String mail, String password, String firstName, String lastName, String titles) {
}
