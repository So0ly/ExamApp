package pbs.report;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.reactive.mutiny.Mutiny;
import pbs.examiner.ExaminerService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private static final Logger LOG = Logger.getLogger(ReportServiceImpl.class);
    private final ExaminerService examinerService;

    public Uni<List<Report>> getAllReports(){
        LOG.trace("getAllReports");
        return Report.listAll();
    }

    @WithTransaction
    public Uni<Report> add(Report report) {
        LOG.trace("addReport");
        return examinerService.getCurrentExaminer().
                chain(examiner -> {
                    report.examiner = examiner;
                    return report.persistAndFlush();
                });
    }

    public Uni<Report> getReportById(Long id) {
        LOG.trace("getReportById");
        return examinerService.getCurrentExaminer()
                .chain(user -> Report.<Report>findById(id)
                        .onItem().ifNull().failWith(() -> new ObjectNotFoundException(id, "Report"))
                        .onItem().invoke(report -> {
                            if (!user.equals(report.examiner)) {
                                throw new UnauthorizedException("You are not allowed to update this project");
                            }
                        }));
    }

    public Uni<List<Report>> getExaminerReports() {
        LOG.trace("getExaminerReports");
        return examinerService.getCurrentExaminer().
                chain(ex -> Report.list("examiner", ex));
    }

    public Uni<List<Report>> getStudentReports(Long id) {
        LOG.trace("getStudentReports");
        return Report.list("studentId", id);
    }

    @WithTransaction
    public Uni<Void> delete(Long id) {
        return Report.delete("id", id).replaceWithVoid();
    }

    @WithTransaction
    public Uni<Report> update(Report report) {
        return Report.findById(report.id)
                .chain(r -> Report.getSession())
                .chain(s -> s.merge(report));
    }

    public Uni<Void> generatePDF(Uni<List<Report>> reportList){
        return reportList
                .onItem()
                .transformToUni(reports -> {
                    if (!reports.isEmpty()) {
                        String reporter = reports.get(0).examiner.id.toString();
                        try (PDDocument pdDocument = new PDDocument()) {
                            reports.forEach(report -> {
                                PDPage page = new PDPage();
                                pdDocument.addPage(page);
                                addPageFromTemplate(pdDocument, report, page, reporter);
                            });

                            String fileName = String.format(
                                    "%s-%s.pdf",
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                    reporter
                            );
                            pdDocument.save(fileName);
                            return Uni.createFrom().item(reports);
                        } catch (IOException e) {
                            LOG.trace(e.getStackTrace());
                            LOG.error(e.getMessage());
                            throw new RuntimeException(e);
                        }
                    } else {
                        LOG.warn("Report list is empty. PDF generation aborted");
                        return Uni.createFrom().item(reports);
                    }
                })
                .onItem()
                .ignore().andContinueWithNull();
    }
//        try (PDDocument pdDocument = new PDDocument()){
//            if (!reportList.subscribe().asCompletionStage().get().isEmpty()) {
//                String reporter = reportList.subscribe().asCompletionStage().get().get(0).examiner.id.toString();
//                reportList.await().indefinitely().forEach(report -> {
//                    PDPage page = new PDPage();
//                    pdDocument.addPage(page);
//                    addPageFromTemplate(pdDocument, report, page, reporter);
//                });
//                String fileName = String.format("%s-%s.pdf", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), reporter);
//                pdDocument.save(fileName);
//            } else {
//                LOG.warn("Report list is empty. PDF generation aborted");
//            }
//        } catch (IOException e){
//            LOG.trace(e.getStackTrace());
//            LOG.error(e.getMessage());
//            throw new RuntimeException(e);
//        } catch (ExecutionException e) {
//            LOG.trace(e.getStackTrace());
//            LOG.error(e.getMessage());
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            LOG.trace(e.getStackTrace());
//            LOG.error(e.getMessage());
//            throw new RuntimeException(e);
//        }
//        return Uni.createFrom().voidItem();
//    }

    private void addPageFromTemplate(PDDocument pdDocument, Report report, PDPage page, String reporter) {
        try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page)) {
            URL imgURL = ReportServiceImpl.class.getClassLoader().getResource("/imgs/PBSlogo.png");
            if (imgURL != null) {
                try {
                    PDImageXObject img = PDImageXObject.createFromFile(imgURL.getPath(), pdDocument);
                    PDRectangle size = page.getMediaBox();
                    contentStream.drawImage(img,size.getLowerLeftX()+40,size.getUpperRightY()-148, 166, 148);
                } catch (IOException e) {
                    LOG.trace(e.getStackTrace());
                    LOG.error("Error loading image: " + e.getMessage());
                }
            }

            contentStream.beginText();
            contentStream.setFont(PDType0Font.load(pdDocument, new File(ReportServiceImpl.class.getResource("/Arial-Unicode-Regular.ttf").getPath())), 12);
            contentStream.setLeading(14.5f);
            contentStream.newLineAtOffset(25, 625);
            contentStream.showText("Data zaliczenia: " + report.examDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            contentStream.newLine();
            contentStream.showText("Numer Indeksu: " + report.student.index);
            contentStream.newLine();
            contentStream.showText("Imię i nazwisko studenta: " + report.student.firstName + " " + report.student.lastName);
            contentStream.newLine();
            contentStream.showText("Nazwa przedmiotu: "+ report.className);
            contentStream.newLine();
            Integer[] iter = {1};
            Uni<Map<String,Float>> questions = Mutiny.fetch(report.questions);
            questions.subscribe().asCompletionStage().get().forEach((question, grade) ->{
                try {
                    contentStream.showText(iter[0].toString() + ". " + question + " - " + grade);
                    contentStream.newLine();
                    iter[0]++;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            contentStream.showText("Ocena końcowa: " + report.finalGrade);
            contentStream.newLine();
            contentStream.showText("Egzaminator: " + report.examiner.toString());
            contentStream.endText();
        } catch (IOException e) {
            LOG.trace(e.getStackTrace());
            LOG.error(e.getMessage());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
