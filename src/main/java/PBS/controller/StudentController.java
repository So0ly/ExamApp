package PBS.controller;

import PBS.model.Student;
import PBS.service.StudentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
@Path("/student")
public class StudentController {
    private final StudentService studentService;

    @GET
    @Operation(operationId = "getAllStudents",
            description = "Returns all available students. ADMIN only.")
    @Produces({"application/json"})
    public Response getAllStudents(){
        List<Student> students = studentService.getStudents();
        return Response.status(Response.Status.OK).entity(students).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Operation(operationId = "getStudentById",
            description = "Returns a single student.")
    @Path("/{id}")
    @Produces("application/json")
    public Response getStudentById(@PathParam("id") Long id){
        Optional<Student> student = studentService.getStudentById(id);
        if (student.isEmpty()){
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.OK).entity(student.get()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @DELETE
    @Operation(operationId = "deleteStudentById",
            description = "Deletes a single student.")
    @Path("/{id}")
    @Produces("application/json")
    public Response deleteStudentById(@PathParam("id") Long id){
        if (studentService.deleteStudent(id)){
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Operation(operationId = "addStudent",
            description = "Creates a single student.")
    @Produces("application/json")
    public Response addStudent(@QueryParam("firstName") String firstName,
                                @QueryParam("lastName") String  lastName){
        Student student = new Student(firstName, lastName);
        studentService.addStudent(student);
        return Response.status(Response.Status.OK).entity(student).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Operation(operationId = "addStudentsCSV",
            description = "Creates a list of students via an uploaded CSV file.")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public Response addStudentCSV(FormData fileData){
        List<Student> students = studentService.addStudentCSV(fileData);

        return Response.status(Response.Status.OK).entity(students).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
