package pbs.report;

import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    Uni<List<Report>> getAllReports();
    Uni<Report> add(Report report);
    Uni<Report> getReportById(Long id);
    Uni<List<Report>> getExaminerReports();
    Uni<List<Report>> getStudentReports(Long id);
    Uni<Void> delete(Long id);
    Uni<Report> update(Report report);
    Uni<Void> generatePDF(Uni<List<Report>> reportList);
}
