package pbs.report;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import org.jboss.resteasy.reactive.ResponseStatus;
import pbs.student.Student;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import pbs.student.StudentService;

import java.util.*;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@ApplicationScoped
@RequiredArgsConstructor
@Path("/report")
@RolesAllowed("user")
public class ReportController {

    private final ReportService reportService;
    private final StudentService studentService;

    @GET
    @Operation(operationId = "getAllReports",
               description = "Returns all available reports. ADMIN only.")
    @Produces({"application/json"})
    @Path("/all")
    @RolesAllowed("admin")
    public Uni<List<Report>> getAllReports(){
        return reportService.getAllReports();
    }

    @GET
    @Operation(operationId = "getReportById",
            description = "Returns a single report by its ID. ADMIN only.")
    @Path("/{Id}")
    @Produces({"application/json"})
    @RolesAllowed("admin")
    public Uni<Report> getReportById(@PathParam("Id") Long id) {
        return reportService.getReportById(id);
    }

    @GET
    @Operation(operationId = "getExaminerReports",
            description = "Returns all available reports for a provided examiner.")
    @Produces({"application/json"})
    public Uni<List<Report>> getExaminerReports() {
        return reportService.getExaminerReports();
    }

    @POST
    @Operation(operationId = "addReport",
            description = "Adds a report. It's assigned to a logged in examiner.")
    @ResponseStatus(201)
    @Consumes("application/json")
    public Uni<Report> addReport(Report report) {
        return reportService.add(report);
    }

    @DELETE
    @Operation(operationId = "deleteReport",
            description = "Remove a report. ADMIN only.")
    @Path("/{Id}")
    public Uni<Void> deleteReport(@PathParam("Id") Long id){
        return reportService.delete(id);
    }

    @PUT
    @Operation(operationId = "updateReport",
            description = "Modify a report by a given ID or add it if it doesn't exist")
    @Path("/{Id}")
    @Consumes("application/json")
    public Uni<Report> update(@PathParam("Id") Long id, Report report){
        report.id = id;
        return reportService.update(report);
    }

    @GET
    @Operation(operationId = "generatePdf")
    @Path("/generate")
    public Uni<Void> generatePdf(){
        Uni<List<Report>> reports = reportService.getAllReports();
        return reportService.generatePDF(reports);
    }
}
