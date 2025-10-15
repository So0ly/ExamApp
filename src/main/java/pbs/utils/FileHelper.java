package pbs.utils;

import org.jboss.logging.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class FileHelper {
    private static final Logger LOGGER = org.jboss.logging.Logger.getLogger(FileHelper.class);
    public static File getResourcesFile(String fileName) {
        LOGGER.debugf("Reading file: {}... ", fileName);
        return new File(Objects.requireNonNull(FileHelper.class.getClassLoader().getResource(fileName)).getFile());
    }
}
