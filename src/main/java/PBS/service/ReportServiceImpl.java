package PBS.service;

import PBS.model.Report;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private static final Logger LOG = Logger.getLogger(ReportServiceImpl.class);
    private final ExaminerService examinerService;

    public List<Report> getAllReports(){
        LOG.trace("getAllReports");
        return Report.listAll();
    }

    @Transactional
    public void addReport(Report report) {
        LOG.trace("addReport");
        report.persist();
    }

    public Optional<Report> getReportById(Long id) {
        LOG.trace("getReportById");
        return Report.findByIdOptional(id);
    }

    public List<Report> getExaminerReports(Long id) {
        LOG.trace("getExaminerReports");
//        return Report.list("SELECT r FROM Report r WHERE r.examinerId = :examinerId");
        return Report.list("examinerId", id);
    }

    @Transactional
    public boolean deleteReport(Long id) {
        return Report.deleteById(id);
    }

    @Transactional
    public Report modifyOrAddReport(Long id, Report newData) {
        Optional<Report> report = Report.findByIdOptional(id);
        if (report.isEmpty()){
            Report.persist(newData);
            return newData;
        }
        Report reportData = report.get();
        reportData.studentID = newData.studentID;
        reportData.firstName = newData.firstName;
        reportData.lastName = newData.lastName;
        reportData.questions = newData.questions;
        reportData.finalGrade = newData.finalGrade;
        reportData.examinerId = newData.examinerId;
        reportData.persist();


        return reportData;
    }

    public void generatePDF(List<Report> reportList){
        try (PDDocument pdDocument = new PDDocument()){
            String reporter = reportList.get(0).examinerId.toString();
            reportList.forEach(report -> {
                PDPage page = new PDPage();
                pdDocument.addPage(page);
                addPageFromTemplate(pdDocument, report, page);
            });
            String fileName = String.format("%s-%s.pdf", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),reporter);
            pdDocument.save(fileName);
        } catch (IOException e){
            LOG.trace(e.getStackTrace());
            System.out.println(e.getMessage());
        }
    }

    private void addPageFromTemplate(PDDocument pdDocument, Report report, PDPage page) {
        try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page)) {
            URL imgURL = ReportServiceImpl.class.getClassLoader().getResource("/imgs/PBSlogo.png");
            if (imgURL != null) {
                try {
                    PDImageXObject img = PDImageXObject.createFromFile(imgURL.getPath(), pdDocument);
                    PDRectangle size = page.getMediaBox();
                    contentStream.drawImage(img,size.getLowerLeftX()+40,size.getUpperRightY()-148, 166, 148);
                } catch (IOException e) {
                    LOG.trace(e.getStackTrace());
                    System.out.println("Error loading image: " + e.getMessage());
                }
            }

            contentStream.beginText();
            contentStream.setFont(PDType0Font.load(pdDocument, new File(ReportServiceImpl.class.getResource("/Arial-Unicode-Regular.ttf").getPath())), 12);
            contentStream.setLeading(14.5f);
            contentStream.newLineAtOffset(25, 725);
            contentStream.showText("Data zaliczenia: " + report.examDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            contentStream.newLine();
            contentStream.showText("Numer Indeksu: " + report.studentID);
            contentStream.newLine();
            contentStream.showText("Imię i nazwisko studenta: " + report.firstName + " " + report.lastName);
            contentStream.newLine();
            Integer[] iter = {1};
            report.questions.forEach((question, grade) ->{
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
            contentStream.showText("Egzaminator: " + examinerService.getExaminerById(report.examinerId).get());
            contentStream.endText();
        } catch (IOException e) {
            LOG.trace(e.getStackTrace());
            System.out.println(e.getMessage());
        }

    }
}
