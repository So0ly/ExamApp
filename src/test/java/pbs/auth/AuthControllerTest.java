package pbs.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class AuthControllerTest {

    @Test
    void loginValidCredentials() {
        String token = given()
                .body("{\"mail\":\"admin@pbs.edu.pl\",\"password\":\"quarkus\"}")
                .contentType(ContentType.JSON)
                .when().post("http://localhost:8080/api/auth/login")
                .then()
                .statusCode(200)
                .body(not(emptyString()))
                .extract().asString();
        System.out.println(token);
    }

    @Test
    void loginInvalidCredentials() {
        given()
                .body("{\"mail\":\"admin@pbs.edu.pl\",\"password\":\"not-quarkus\"}")
                .contentType(ContentType.JSON)
                .when().post("http://localhost:8080/api/auth/login")
                .then()
                .statusCode(401);
    }
}