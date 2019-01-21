package replacer.scheduletasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.function.Predicate;

import static replacer.utils.Constants.BACKUP_DAYS_CRON_JOB;
import static replacer.utils.Constants.BACKUP_DAYS_LIMIT;
import static replacer.utils.Uitls.createTempsDirectoryPath;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Scheduled(cron = BACKUP_DAYS_CRON_JOB)
    public void reportCurrentTime() throws Exception {
        Files.walk(Paths.get(createTempsDirectoryPath()))
                .filter(isOldFile())
                .forEach(this::deleteFile);
    }

    private void deleteFile(Path p) {
        boolean isDeleted = p.toFile().delete();
        if (isDeleted) {
            log.info("File has been deleted {}", p.toFile().getName());
        } else {
            log.info("Error during deleting file {}", p.toFile().getName());
        }
    }

    private Predicate<Path> isOldFile() {
        return p -> !Files.isDirectory(p) && new Date().getTime() - p.toFile().lastModified() > BACKUP_DAYS_LIMIT * 24 * 60 * 60 * 1000;
    }
}
