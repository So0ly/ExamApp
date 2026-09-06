package pbs.report;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.ObjectNotFoundException;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import pbs.examiner.ExaminerService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import org.jboss.logging.Logger;
import pbs.model.CSVQuestionBean;
import pbs.model.IdList;
import pbs.student.StudentService;
import pbs.utils.CSVHelper;
import pbs.utils.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private static final Logger LOG = Logger.getLogger(ReportServiceImpl.class);
    @ConfigProperty(name = "PDF_STORAGE_PATH")
    Path PDF_PATH;

    @ConfigProperty(name = "AUDIO_STORAGE_PATH")
    Path AUDIO_PATH;

    @ConfigProperty(name = "pdf.font.path")
    String fontPath;

    private final ExaminerService examinerService;
    private final StudentService studentService;
    private final WhisperTranscriptionService whisperTranscriptionService;
    private final PgPool pgPool;

    public Uni<List<Report>> getAllReports(){
        LOG.trace("getAllReports");
        return Report.listAll();
    }

    @WithTransaction
    public Uni<Report> add(Report report) {
        LOG.trace("addReport");
        report.setFinalGrade();
        String index = report.student.index;
        return studentService.getStudentByIndex(index)
                    .onItem().ifNull().switchTo(() -> studentService.addStudent(report.student))
                    .chain(student -> {
                        report.student = student;
                        return examinerService.getCurrentExaminer();
                    })
                    .chain(examinerService::getCurrentExaminer)
                    .chain(examiner -> {
                    report.examiner = examiner;
                    return report.persistAndFlush();
                });
    }

    @WithTransaction
    public Uni<Report> addAudio(Long id, File audio) {
        LOG.trace(">>>addAudio");
        return examinerService.getCurrentExaminer()
                .chain(user -> Report.<Report>findById(id)
                        .onItem().ifNull().failWith(() -> new ObjectNotFoundException(id, "Report"))
                        .onItem().invoke(report -> {
                            if (!user.equals(report.examiner)) {
                                throw new UnauthorizedException("You are not allowed to update this project");
                            }
                        }))
                .chain(report -> {
                    String fileName = String.format(
                            "%s-%s.wav",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            id
                    );
                    Path audioPath = AUDIO_PATH.resolve(fileName);
                    try {
                        Files.copy(audio.toPath(), audioPath);
                        report.audioURL = fileName;
                        return report.persistAndFlush()
                                .map(saved -> {
                                    Report savedReport = (Report) saved;
                                    whisperTranscriptionService.transcribeInBackground(savedReport.id, audioPath);
                                    return savedReport;
                                });
                    } catch (Exception e) {
                        LOG.error("Error saving audio file", e);
                        throw new RuntimeException("Error saving audio file", e);
                    }
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

    public Uni<List<Report>> getReportsByIds(List<Long> ids) {
        LOG.trace("getReportsByIds");
        return examinerService.getCurrentExaminer()
                .chain(user -> Report.list("id in ?1 and examiner = ?2", ids, user));
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

    public Uni<Void> saveTranscription(Long reportId, String transcription) {
        return pgPool.preparedQuery("UPDATE reports SET transcription = $1 WHERE id = $2")
                .execute(Tuple.of(transcription, reportId))
                .replaceWithVoid();
    }

    public Uni<List<CSVQuestionBean>> parseQuestionCSV(File fileData) {
        List<CSVQuestionBean> questions = CSVHelper.parseCSVIntoBeanList(fileData, CSVQuestionBean.class);
        return Uni.createFrom().item(questions);
    }

    public Uni<String> generatePDF(IdList idList){
        List<Long> ids = idList.ids();
        Uni<List<Report>> reportList = getReportsByIds(ids);
        return reportList
                .onItem()
                .transformToUni(reports -> {
                    if (!reports.isEmpty()) {
                        String reporter = reports.getFirst().examiner.id.toString();
                        try (PDDocument pdDocument = new PDDocument()) {
                            reports.forEach(report -> {
                                PDPage page = new PDPage();
                                pdDocument.addPage(page);
                                addPageFromTemplate(pdDocument, report, page);
                                addTranscriptPages(pdDocument, report);
                            });

                            String fileName = String.format(
                                    "%s-%s.pdf",
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                    reporter
                            );
                            Path filePath = PDF_PATH.resolve(fileName);
                            pdDocument.save(filePath.toFile());
                            LOG.info("PDF file generated: " + filePath);
                            return Uni.createFrom().item(fileName);
                        } catch (IOException e) {
                            LOG.error("Something went wrong with PDF creation", e);
                            return Uni.createFrom().failure(new RuntimeException("Failed to generate PDF", e));
                        } catch (Exception e) {
                            LOG.error("Something went wrong with PDF creation", e);
                            return Uni.createFrom().failure(new RuntimeException("Failed to generate PDF", e));
                        }
                    } else {
                        LOG.warn("Report list is empty. PDF generation aborted");
                        return Uni.createFrom().nullItem();
                    }
                });
    }

    private void addPageFromTemplate(PDDocument pdDocument, Report report, PDPage page) {
        LOG.debugf("Adding page to PDF {}", report.id);
        try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page)) {
            File imgFile = FileHelper.getResourcesFile("/imgs/PBSlogo.png");
                try {
                    PDImageXObject img = PDImageXObject.createFromFileByContent(imgFile, pdDocument);
                    PDRectangle size = page.getMediaBox();
                    contentStream.drawImage(img,size.getLowerLeftX()+40,size.getUpperRightY()-148, 166, 148);
                } catch (IOException e) {
                    LOG.trace(e.getStackTrace());
                    LOG.error("Error loading image: " + e.getMessage());
                }

            PDFont font = PDType0Font.load(pdDocument, FileHelper.getResourcesFile(fontPath));
            contentStream.beginText();
            contentStream.setFont(font, 12);
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
            int iter = 1;
            for (ReportQuestions question : report.reportQuestions) {
                contentStream.showText(iter++ + ". " + question.getQuestion() + " - " + question.getGrade());
                contentStream.newLine();
            }
            contentStream.showText("Ocena końcowa: " + report.finalGrade);
            contentStream.newLine();
            contentStream.showText("Czas trwania: " + report.examDuration);
            contentStream.newLine();
            contentStream.showText("Egzaminator: " + report.examiner.toString());
            contentStream.newLineAtOffset(20, -450);
            contentStream.showText(".".repeat(40) + " ".repeat(65) + ".".repeat(40));
            contentStream.newLine();
            contentStream.showText("  (Podpis egzaminatora)" + " ".repeat(72) + "(Podpis studenta)");
            contentStream.endText();
        } catch (IOException e) {
            LOG.trace(e.getStackTrace());
            LOG.error(e.getMessage());
        }
    }

    private void addTranscriptPages(PDDocument document, Report report) {
        PDFont font;
        PDRectangle pageSize = new PDPage().getMediaBox();
        try {
            font = PDType0Font.load(document, FileHelper.getResourcesFile(fontPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load PDF font", e);
        }

        String transcription = report.transcription == null ? "" : report.transcription.trim();
        List<String> lines = wrapText(
                transcription.isEmpty() ? "Transcription is not available yet." : transcription,
                font, 12, pageSize.getWidth() - 80
        );
        int linesPerPage = (int) ((pageSize.getHeight() - 80) / 14.5f);

        int start = 0;
        int pageNumber = 0;
        while (start < lines.size()) {
            int pageCapacity = pageNumber == 0 ? linesPerPage - 1 : linesPerPage;
            int end = Math.min(start + pageCapacity, lines.size());
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(40, pageSize.getHeight() - 40);
                if (pageNumber == 0) {
                    contentStream.showText("Transkrypcja:");
                    contentStream.newLine();
                }
                for (int line = start; line < end; line++) {
                    contentStream.showText(lines.get(line));
                    contentStream.newLine();
                }
                contentStream.endText();
            } catch (IOException e) {
                throw new RuntimeException("Failed to write transcription page", e);
            }
            start = end;
            pageNumber++;
        }
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) {
        List<String> lines = new java.util.ArrayList<>();
        for (String paragraph : text.replace("\r\n", "\n").split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split("\\s+")) {
                if (word.isEmpty()) {
                    continue;
                }
                String candidate = line.isEmpty() ? word : line + " " + word;
                try {
                    if (font.getStringWidth(candidate) / 1000 * fontSize <= maxWidth) {
                        line.setLength(0);
                        line.append(candidate);
                    } else {
                        if (!line.isEmpty()) {
                            lines.add(line.toString());
                        }
                        line.setLength(0);
                        line.append(word);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to measure transcription text", e);
                }
            }
            lines.add(line.toString());
        }
        return lines;
    }

    public Uni<Response> getFile(String filename, String fileType) {
        Path path;
        if (fileType.equals("PDF")){
            path = PDF_PATH;
        } else {
            path = AUDIO_PATH;
        }
        File file = new File(path + "/" + filename);
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
