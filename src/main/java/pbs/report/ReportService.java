package pbs.report;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import pbs.model.CSVQuestionBean;

import java.io.File;
import java.util.List;

public interface ReportService {
    Uni<List<Report>> getAllReports();
    Uni<Report> add(Report report);
    Uni<Report> addAudio(Long id, File audio);
    Uni<Report> getReportById(Long id);
    Uni<List<Report>> getReportsByIds(List<Long> ids);
    Uni<List<Report>> getExaminerReports();
    Uni<List<Report>> getStudentReports(Long id);
    Uni<Void> delete(Long id);
    Uni<Report> update(Report report);
    Uni<String> generatePDF(Uni<List<Report>> reportList);
    Uni<Response> getFile(String fileName, String fileType);
    Uni<List<CSVQuestionBean>> parseQuestionCSV(File fileData);
}
