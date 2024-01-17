package pbs.report;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.reactive.mutiny.Mutiny;
import pbs.examiner.ExaminerService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import org.jboss.logging.Logger;
import pbs.model.Question;
import pbs.student.Student;
import pbs.student.StudentService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private static final Logger LOG = Logger.getLogger(ReportServiceImpl.class);
    @ConfigProperty(name = "PDF_STORAGE_PATH")
    String PDF_PATH;
    @ConfigProperty(name = "PDF_ACCESS_URL_BASE")
    String PDF_URL;

    @ConfigProperty(name = "AUDIO_STORAGE_PATH")
    Path AUDIO_PATH;

    @ConfigProperty(name = "AUDIO_ACCESS_URL_BASE")
    String AUDIO_URL;

    private final ExaminerService examinerService;
    private final StudentService studentService;

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
        LOG.info("addAudio");
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
                        LOG.info("AudioPath" + audioPath.toString());
                        LOG.info("Audio" + audio.toString() + "|||" + audio.getAbsolutePath());
                        try {
                            Files.copy(audio.toPath(), audioPath);
                            report.audioURL = audioPath.toString(); //TODO
                            return report.persistAndFlush();
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

    public Uni<List<Question>> parseQuestionCSV(File fileData) {
        return Uni.createFrom().item(() -> {List<Question> questions = new ArrayList<>();
            try (FileReader fileReader = new FileReader(fileData)) {
                try (CSVReader reader = new CSVReader(fileReader)) {
                    questions = reader.readAll().stream().map(row -> new Question(row[0])).toList();
                } catch (CsvException e) {
                    LOG.error(e);
                }
            } catch (FileNotFoundException e) {
                LOG.error(e);
            } catch (IOException e) {
                LOG.error(e);
            }
            return questions;
        });
    }

    public Uni<String> generatePDF(Uni<List<Report>> reportList){
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
                            String filePath = PDF_PATH + "/" + fileName;
                            pdDocument.save(filePath);
                            LOG.info("PDF file generated: " + filePath);
                            return Uni.createFrom().item(fileName);
                        } catch (IOException e) {
                            LOG.trace(e.getStackTrace());
                            LOG.error(e.getMessage());
                            throw new RuntimeException(e);
                        }
                    } else {
                        LOG.warn("Report list is empty. PDF generation aborted");
                        return Uni.createFrom().nullItem();
                    }
                });
    }

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
            contentStream.showText("Czas trwania: " + report.examDuration);
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
