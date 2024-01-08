package pbs.pdfGen;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import pbs.examiner.ExaminerService;
import pbs.report.ReportServiceImpl;

import java.io.File;
import java.io.FileNotFoundException;

@ApplicationScoped
@RequiredArgsConstructor
@Path("/report/pdf")
public class PdfGen {
    private final ExaminerService examinerService;
    private static final Logger LOG = Logger.getLogger(ReportServiceImpl.class);

    @ConfigProperty(name = "PDF_STORAGE_PATH")
    String PDF_PATH;

    @GET
    @Path("/{filename}")
    @Produces("application/pdf")
    public Uni<Response> getFile(@PathParam("filename") String filename) {
        File file = new File(PDF_PATH + "/" + filename);
        if (file.exists()) {
            LOG.info("File found: " + file.getPath());
            return Uni.createFrom().item(Response.ok(file)
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .build());
        } else {
            return Uni.createFrom().failure(new FileNotFoundException("File not found"));
        }
    }
}
