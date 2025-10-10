package pbs.utils;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.*;
import java.util.List;
import org.jboss.logging.Logger;
import org.apache.commons.io.input.BOMInputStream;

public class CSVHelper {
    private static final Logger LOG = Logger.getLogger(CSVHelper.class);

    public static <T> List<T> parseCSVIntoBeanList(File fileData, Class<T> beanClass){
        try (FileInputStream fileStream = new FileInputStream(fileData);
             BOMInputStream bomInput = BOMInputStream.builder().setInputStream(fileStream).get();
             InputStreamReader inputStreamReader = new InputStreamReader(bomInput);) {
            return new CsvToBeanBuilder<T>(inputStreamReader)
                    .withType(beanClass)
                    .withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
        } catch (IOException e) {
            LOG.error("Something went wrong while reading CSV file", e);
            throw new RuntimeException(e);
        }
    }
}
