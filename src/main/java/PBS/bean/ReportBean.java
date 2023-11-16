package PBS.bean;

import PBS.model.Report;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ReportBean {
    @Inject
    EntityManager em;

    @Transactional
    @PostConstruct
    public void createReport() {
        Report report = new Report();
        report.firstName = "aaa";
        em.persist(report);
    }
}
