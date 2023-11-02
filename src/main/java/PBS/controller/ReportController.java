package PBS.controller;

import PBS.model.Report;
import PBS.service.ReportService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
@Path("/report")
public class ReportController {

    private final ReportService reportService;

    @GET
    @Operation(operationId = "getAllReports",
               description = "Returns all available reports. ADMIN only.")
    @Produces({"application/json", "text/plain"})
    public List<Report> getAllReports(){

        return reportService.getAllReports();
    }

    @POST
    @Operation(operationId = "addReport",
            description = "Adds a report. It's assigned to a logged in examinator.")
    @Transactional
    public void addReport() {
        Report report = new Report();
        reportService.addReport(report);
    }

    @GET
    @Operation(operationId = "getReportById",
            description = "Returns a single report by its ID. ADMIN only.")
    @Path("/{Id}")
    public Optional<Report> getReportById(@PathParam("Id") Long id) { //TODO only ADMIN account
        return reportService.getReportById(id);
    }

    @GET
    @Operation(operationId = "getExaminatorReports",
            description = "Returns all available reports for a provided examinator.")
    @Path("/byExId/{Id}")
    public List<Report> getExaminatorReports(@PathParam("Id") Long id) {
        return reportService.getExaminatorReports(id);
    }
}
