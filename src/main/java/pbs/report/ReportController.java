package pbs.report;

import io.smallrye.mutiny.Uni;
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
public class ReportController {

    private final ReportService reportService;
    private final StudentService studentService;

    @GET
    @Operation(operationId = "getAllReports",
               description = "Returns all available reports. ADMIN only.")
    @Produces({"application/json"})
    public Uni<List<Report>> getAllReports(){
        return reportService.getAllReports();
    }

    @GET
    @Operation(operationId = "getReportById",
            description = "Returns a single report by its ID. ADMIN only.")
    @Path("/{Id}") //TODO @Path("self")
    @Produces({"application/json"})
    public Uni<Report> getReportById(@PathParam("Id") Long id) { //TODO only ADMIN account
        return reportService.getReportById(id);
    }

    @GET
    @Operation(operationId = "getExaminerReports",
            description = "Returns all available reports for a provided examiner.")
    @Path("/byExId/{Id}")
    @Produces({"application/json"})
    public Uni<List<Report>> getExaminerReports(@PathParam("Id") Long id) {
        return reportService.getExaminerReports(id);
    }

    @POST
    @Operation(operationId = "addReport",
            description = "Adds a report. It's assigned to a logged in examiner.")
    @ResponseStatus(201)
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

    @PATCH
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
    public void generatePdf(){
        Uni<List<Report>> reports = reportService.getAllReports();
        reportService.generatePDF(reports);
    }
}
