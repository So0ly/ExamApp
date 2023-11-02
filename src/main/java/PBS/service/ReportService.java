package PBS.service;

import PBS.model.Report;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    List<Report> getAllReports();
    void addReport(Report report);
    Optional<Report> getReportById(Long id);
    List<Report> getExaminatorReports(Long id);
}
