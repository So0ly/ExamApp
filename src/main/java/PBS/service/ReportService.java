package PBS.service;

import PBS.model.Report;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    List<Report> getAllReports();
    void addReport(Report report);
    Optional<Report> getReportById(Long id);
    List<Report> getExaminerReports(Long id);
    boolean deleteReport(Long id);
    Report modifyOrAddReport(Long id, Report report);


    void generatePDF(List<Report> reportList);
}
