package pbs.student;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import org.jboss.resteasy.reactive.RestForm;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.io.File;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
@Path("/student")
@RolesAllowed("user")
public class StudentController {
    private final StudentService studentService;

    @GET
    @Operation(operationId = "getAllStudents",
            description = "Returns all available students. ADMIN only.")
    @Produces({"application/json"})
    @RolesAllowed("admin")
    public Uni<List<Student>> getAllStudents(){
        return studentService.getStudents();
    }

    @GET
    @Operation(operationId = "getStudentById",
            description = "Returns a single student. ADMIN only")
    @Path("/{id}")
    @Produces("application/json")
    @RolesAllowed("admin")
    public Uni<Student> getStudentById(@PathParam("id") Long id){
        return studentService.getStudentById(id);
    }

    @GET
    @Operation(operationId = "getStudentByIndex",
            description = "Returns a single student by the index value.")
    @Path("/index/{index}")
    @Produces("application/json")
    public Uni<Student> getStudentByIndex(@PathParam("index") String  index){
        return studentService.getStudentByIndex(index);
    }

    @DELETE
    @Operation(operationId = "deleteStudentById",
            description = "Deletes a single student.")
    @Path("/{id}")
    @Produces("application/json")
    public Uni<Void> deleteStudentById(@PathParam("id") Long id){
        return studentService.deleteStudent(id);
    }

    @POST
    @Operation(operationId = "addStudent",
            description = "Creates a single student.")
    @Produces("application/json")
    public Uni<Student> addStudent(Student student){
        return studentService.addStudent(student);
    }

    @PUT
    @Operation(operationId = "updateStudent",
                description = "Updates a single student")
    public Uni<Student> updateStudent(Student student){
        return studentService.update(student);
    }

    @POST
    @Path("/file")
    @Operation(operationId = "addStudentsCSV",
            description = "Creates a list of students via an uploaded CSV file.")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public Uni<List<Student>> addStudentCSV(@RestForm File file){
        return studentService.addStudentsCSV(file);
    }
}
