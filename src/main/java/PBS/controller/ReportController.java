package PBS.controller;

import PBS.model.Report;
import PBS.service.ReportService;
import PBS.service.Responser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.*;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@ApplicationScoped
@RequiredArgsConstructor
@Path("/report")
public class ReportController {

    private final ReportService reportService;

    @GET
    @Operation(operationId = "getAllReports",
               description = "Returns all available reports. ADMIN only.")
    @Produces({"application/json"})
    public Response getAllReports(){
        List<Report> reports = reportService.getAllReports();
        return Response.status(Response.Status.OK).entity(reports).type(APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Operation(operationId = "getReportById",
            description = "Returns a single report by its ID. ADMIN only.")
    @Path("/{Id}")
    @Produces({"application/json"})
    public Optional<Report> getReportById(@PathParam("Id") Long id) { //TODO only ADMIN account
        return reportService.getReportById(id);
    }

    @GET
    @Operation(operationId = "getExaminerReports",
            description = "Returns all available reports for a provided examiner.")
    @Path("/byExId/{Id}")
    @Produces({"application/json"})
    public List<Report> getExaminerReports(@PathParam("Id") Long id) {
        return reportService.getExaminerReports(id);
    }

    @POST
    @Operation(operationId = "addReport",
            description = "Adds a report. It's assigned to a logged in examiner.")
    public void addReport(@QueryParam("studentId") String studentId) {
        Report report = new Report();
        reportService.addReport(report);
    }

    @DELETE
    @Operation(operationId = "deleteReport",
            description = "Remove a report. ADMIN only.")
    @Path("/{Id}")
    public Response deleteReport(@PathParam("Id") Long id){
        Response.Status response=Response.Status.OK;
        boolean isDeleted= reportService.deleteReport(id);
        if (isDeleted) {
            response = Response.Status.NOT_FOUND;
        }

        return Response.status(response).type("APPLICATION_JSON_TYPE").build();
    }

    @PATCH
    @Operation(operationId = "modifyOrAddReport",
            description = "Modify a report by a given ID or add it if it doesn't exist")
    @Path("/{Id}")
    public Response modifyOrAddReport(@PathParam("Id") Long id, Report changedData){
        Report report = reportService.modifyOrAddReport(id, changedData);
        return Responser.responser(Response.Status.OK, report, APPLICATION_JSON_TYPE);
    }

    @POST
    @Operation(operationId = "generatePdf")
    @Path("/generate")
    public void generatePdf(){
        Map<String, Float> questions = Map.of("aaa", 3.5f, "bbb", 4f);

        List<Report> reports = new ArrayList<>();
        Report report1 = new Report("22", "aa", "bb", questions, 2L);
        Report report2 = new Report("223", "aba", "bcb", questions, 2L);
        reports.add(report1);
        reports.add(report2);
        reportService.generatePDF(reports);
    }
}
