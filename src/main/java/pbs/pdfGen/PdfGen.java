package pbs.pdfGen;

import pbs.examiner.ExaminerService;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@RequiredArgsConstructor
public class PdfGen {
    private static final Logger LOG = Logger.getLogger(PdfGen.class);

    private final ExaminerService examinerService;


}