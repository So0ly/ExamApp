package PBS.controller;

import PBS.model.Examiner;
import PBS.service.ExaminerService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
@Path("/student")
public class ExaminerController {
    private final ExaminerService examinerService;

    @GET
    @Operation(operationId = "getAllExaminers",
            description = "Returns all available examiners. ADMIN only.")
    @Produces({"application/json"})
    public Response getAllExaminers(){
        List<Examiner> examiners = examinerService.getExaminers();
        return Response.status(Response.Status.OK).entity(examiners).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Operation(operationId = "getExaminerById",
            description = "Returns a single examiner.")
    @Path("/{id}")
    @Produces("application/json")
    public Response getExaminerById(@PathParam("id") Long id){
        Optional<Examiner> examiner = examinerService.getExaminerById(id);
        if (examiner.isEmpty()){
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.OK).entity(examiner.get()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @DELETE
    @Operation(operationId = "deleteExaminerById",
            description = "Deletes a single examiner.")
    @Path("/{id}")
    @Produces("application/json")
    public Response deleteExaminerById(@PathParam("id") Long id){
        if (examinerService.deleteExaminer(id)){
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Operation(operationId = "addExaminer",
            description = "Creates a single examiner.")
    @Produces("application/json")
    public Response addExaminer(@QueryParam("firstName") String firstName,
                                   @QueryParam("lastName") String  lastName,
                                   @QueryParam("titles") String titles){
        Examiner examiner = new Examiner(firstName, lastName, titles);
        examinerService.addExaminer(examiner);
        return Response.status(Response.Status.OK).entity(examiner).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
