package pbs.examiner;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ExaminerControllerTest {

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "admin")
    void list() {
        given()
                .when().get("http://localhost:8080/api/examiner")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThanOrEqualTo(1),
                        "[0].mail", is("admin@pbs.edu.pl"),
                        "[0].password", nullValue());
    }

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "admin")
    void create() {
        given()
                .body("{\"mail\":\"test@pbs.edu.pl\",\"password\":\"test\",\"roles\":[\"user\"]}")
                .contentType(ContentType.JSON)
                .when().post("http://localhost:8080/api/examiner")
                .then()
                .statusCode(201)
                .body(
                        "mail", is("test@pbs.edu.pl"),
                        "password", nullValue(),
                        "created", not(emptyString())
                );
    }

    @Test
    @TestSecurity(user = "user@pbs.edu.pl", roles = "user")
    void createUnauthorized() {
        given()
                .body("{\"mail\":\"test-unauthorized@pbs.edu.pl\",\"password\":\"test\",\"roles\":[\"user\"]}")
                .contentType(ContentType.JSON)
                .when().post("http://localhost:8080/api/examiner")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "admin")
    void createDuplicate() {
        given()
                .body("{\"mail\":\"user@pbs.edu.pl\",\"password\":\"test\",\"roles\":[\"user\"]}")
                .contentType(ContentType.JSON)
                .when().post("http://localhost:8080/api/examiner")
                .then()
                .statusCode(409);
    }

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "admin")
    void get() {
        given()
                .when().get("http://localhost:8080/api/examiner/0")
                .then()
                .statusCode(200)
                .body("mail", is("admin@pbs.edu.pl"));
    }

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "admin")
    void getNotFound() {
        given()
                .when().get("http://localhost:8080/api/examiner/2137")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "admin")
    void update() {
        var examiner = given()
                .body("{\"mail\":\"to-update@pbs.edu.pl\",\"password\":\"test\",\"roles\":[\"user\"]}")
                .contentType(ContentType.JSON)
                .when().post("http://localhost:8080/api/examiner")
                .as(Examiner.class);
        examiner.mail = "updated@pbs.edu.pl";
        given()
                .body(examiner)
                .contentType(ContentType.JSON)
                .when().put("http://localhost:8080/api/examiner/" + examiner.id)
                .then()
                .statusCode(200)
                .body(
                        "mail", is("updated@pbs.edu.pl")
                );
    }

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "admin")
    void updateNotFound() {
        given()
                .body("{\"mail\":\"i-dont-exist@pbs.edu.pl\",\"password\":\"pa33\"}")
                .contentType(ContentType.JSON)
                .when().put("http://localhost:8080/api/examiner/1337")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "admin")
    void delete() {
        var toDelete = given()
                .body("{\"mail\":\"to-delete@pbs.edu.pl\",\"password\":\"test\"}")
                .contentType(ContentType.JSON)
                .post("http://localhost:8080/api/examiner")
                .as(Examiner.class);
        given()
                .when().delete("http://localhost:8080/api/examiner/" + toDelete.id)
                .then()
                .statusCode(204);
        assertThat(Examiner.findById(toDelete.id).await().indefinitely(), nullValue());
    }

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "user")
    void getCurrentUser() {
        given()
                .when().get("http://localhost:8080/api/examiner/self")
                .then()
                .statusCode(200)
                .body("mail", is("admin@pbs.edu.pl"));
    }

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "user")
    void changePassword() {
        given()
                .body("{\"currentPassword\": \"quarkus\", \"newPassword\": \"changed\"}")
                .contentType(ContentType.JSON)
                .when().put("http://localhost:8080/api/examiner/self/password")
                .then()
                .statusCode(200);
        assertTrue(BcryptUtil.matches("changed",
                Examiner.<Examiner>findById(0L).await().indefinitely().password)
        );
    }

    @Test
    @TestSecurity(user = "admin@pbs.edu.pl", roles = "user")
    void changePasswordDoesntMatch() {
        given()
                .body("{\"currentPassword\": \"wrong\", \"newPassword\": \"changed\"}")
                .contentType(ContentType.JSON)
                .when().put("http://localhost:8080/api/examiner/self/password")
                .then()
                .statusCode(409);
    }
}