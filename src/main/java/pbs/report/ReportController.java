package pbs.report;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestForm;
import pbs.model.IdList;
import pbs.model.Question;
import pbs.model.UrlResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.io.File;
import java.util.*;

@ApplicationScoped
@RequiredArgsConstructor
@Path("/report")
@RolesAllowed("user")
public class ReportController {

    private final ReportService reportService;

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

    @POST
    @Path("/audio")
    @Operation(operationId = "addAudio",
            description = "Adds audio to a report.")
    @ResponseStatus(201)
    @Consumes("multipart/form-data")
    public Uni<Report> addAudio(@RestForm File audio, @RestForm Long id) {
        return reportService.addAudio(id, audio);
    }

    @DELETE
    @RolesAllowed("admin")
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

    @POST
    @Path("/generate")
    @Operation(operationId = "generatePDF",
            description = "Generates a PDF report")
    @Consumes("application/json")
    public Uni<Response> generatePdf(IdList idList){
        List<Long> ids = idList.ids();
        Uni<List<Report>> reports = reportService.getReportsByIds(ids);
        return reportService.generatePDF(reports)
                .onItem().transform(url -> Response.ok(new UrlResponse(url)).build())
                .onFailure().recoverWithItem(
                        e -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(e.getMessage()).build()
                );
    }

    @GET
    @Path("/pdf/{filename}")
    @Produces("application/pdf")
    @Operation(operationId = "downloadPDF",
            description = "Used to download a generated PDF file.")
    public Uni<Response> downloadPDF(@PathParam("filename") String filename){
        return reportService.getFile(filename, "PDF");
    }

    @GET
    @Path("/audio/{filename}")
    @Produces("audio/mpeg")
    @Operation(operationId = "downloadAudio",
            description = "Used to download an audio recording of the exam.")
    public Uni<Response> downloadAudio(@PathParam("filename") String filename) {
        return reportService.getFile(filename, "Audio");
    }

    @POST
    @Path("/file")
    @Operation(operationId = "parseQuestionCSV",
            description = "Creates a list of questions via an uploaded CSV file.")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public Uni<List<Question>> parseQuestionCSV(@RestForm File file) {return reportService.parseQuestionCSV(file);
    }

}
