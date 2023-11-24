package pbs.examiner;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Optional;

@Path("/examiner")
@RolesAllowed("admin")
@RequiredArgsConstructor
public class ExaminerController {
    private final ExaminerService examinerService;

    @GET
    @Operation(operationId = "getAllExaminers",
            description = "Returns all available examiners. ADMIN only.")
    @Produces({"application/json"})
    public Uni<List<Examiner>> getAllExaminers(){
        return examinerService.list();
    }

    @GET
    @Operation(operationId = "getExaminerById",
            description = "Returns a single examiner.")
    @Path("/{id}")
    @Produces("application/json")
    public Uni<Examiner> getExaminerById(@PathParam("id") Long id){
        return examinerService.findById(id);
    }

    @DELETE
    @Operation(operationId = "deleteExaminerById",
            description = "Deletes a single examiner.")
    @Path("/{id}")
    @Produces("application/json")
    public Uni<Void> deleteExaminerById(@PathParam("id") Long id){
        return examinerService.delete(id);
    }

    @POST
    @Operation(operationId = "addExaminer",
            description = "Creates a single examiner.")
    @Consumes("application/json")
    @Produces("application/json")
//    public Response addExaminer(@QueryParam("email") String email,
//                                @QueryParam("password") String password,
//                                @QueryParam("firstName") String firstName,
//                                @QueryParam("lastName") String  lastName,
//                                @QueryParam("titles") String titles){
//        Examiner examiner = new Examiner(email, password, firstName, lastName, titles);
//        examinerService.add(examiner);
//        return Response.status(Response.Status.OK).entity(examiner).type(MediaType.APPLICATION_JSON_TYPE).build();
    public Uni<Examiner> addExaminer(Examiner examiner){
        return examinerService.add(examiner);
    }

    @PUT
    @Path("self/password")
    @RolesAllowed("user")
    @Consumes("application/json")
    public Uni<Examiner> changePassword(PasswordChange passwordChange) {
        return examinerService.changePassword(passwordChange.currentPassword(),
                        passwordChange.newPassword());
    }
}
