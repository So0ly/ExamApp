package PBS.service;

import PBS.model.Report;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReportServiceImpl implements ReportService{

    public List<Report> getAllReports(){
        return Report.listAll();
    }

    public void addReport(Report report) {

    }

    public Optional<Report> getReportById(Long id) {
        return Report.findByIdOptional(id);
    }

    public List<Report> getExaminatorReports(Long id) {
        return Report.list("SELECT r FROM Report r WHERE r.examinatorId = :examinatorId");
    }
}
