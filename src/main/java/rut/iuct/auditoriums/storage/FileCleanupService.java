package rut.iuct.auditoriums.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class FileCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(FileCleanupService.class);
    private final JdbcTemplate jdbc;
    private final FileStorageService storage;
    private final TransactionTemplate separateTransaction;

    public FileCleanupService(JdbcTemplate jdbc, FileStorageService storage, PlatformTransactionManager transactions) {
        this.jdbc = jdbc;
        this.storage = storage;
        separateTransaction = new TransactionTemplate(transactions);
        separateTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void trackUpload(String key) {
        // Запись переживет rollback и остановку процесса между записью файла и commit БД.
        separateTransaction.executeWithoutResult(status -> jdbc.update(
                "INSERT INTO file_cleanup_task(file_path, next_attempt_at) VALUES (?, CURRENT_TIMESTAMP + INTERVAL '1 hour')", key));
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    try {
                        separateTransaction.executeWithoutResult(tx -> jdbc.update(
                                "UPDATE file_cleanup_task SET next_attempt_at = CURRENT_TIMESTAMP WHERE file_path = ?", key));
                        processPending();
                    } catch (RuntimeException exception) {
                        logger.error("Очистка незавершенной загрузки отложена", exception);
                    }
                }
            }
        });
    }

    public void completeUpload(String key) {
        jdbc.update("DELETE FROM file_cleanup_task WHERE file_path = ?", key);
    }

    public void deleteAfterCommit(String key) {
        jdbc.update("INSERT INTO file_cleanup_task(file_path) VALUES (?) ON CONFLICT(file_path) DO UPDATE SET next_attempt_at = CURRENT_TIMESTAMP", key);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try { processPending(); }
                catch (RuntimeException exception) { logger.error("Очистка файлов отложена", exception); }
            }
        });
    }

    @Scheduled(fixedDelayString = "${app.storage.cleanup-delay-ms:60000}")
    public void processPending() {
        separateTransaction.executeWithoutResult(status -> {
            var tasks = jdbc.queryForList("SELECT id, file_path FROM file_cleanup_task WHERE next_attempt_at <= CURRENT_TIMESTAMP ORDER BY id LIMIT 50 FOR UPDATE SKIP LOCKED");
            for (var task : tasks) {
                String key = (String) task.get("file_path");
                try {
                    String canonical = storage.canonicalKey(key);
                    // Старые относительные и абсолютные пути могут обозначать один файл.
                    boolean referenced = jdbc.queryForList("SELECT file_path FROM auditorium_photo", String.class).stream().anyMatch(path -> {
                        try { return canonical.equals(storage.canonicalKey(path)); }
                        catch (StorageException invalidPath) { return false; }
                    });
                    if (!referenced) storage.delete(key);
                    jdbc.update("DELETE FROM file_cleanup_task WHERE id = ?", task.get("id"));
                } catch (StorageException exception) {
                    logger.error("Не удалось очистить файл; задача {} будет повторена", task.get("id"), exception);
                    jdbc.update("UPDATE file_cleanup_task SET attempts = attempts + 1, next_attempt_at = CURRENT_TIMESTAMP + INTERVAL '1 minute' WHERE id = ?", task.get("id"));
                }
            }
        });
    }
}
