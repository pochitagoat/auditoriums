package rut.iuct.auditoriums.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageTests {
    @TempDir Path directory;

    @Test
    void createsRootStoresGeneratedNameAndSupportsLegacyPathsWithinRoot() {
        var storage = new LocalFileStorageService(directory.resolve("photos").toString());
        String key = storage.newKey("image/png");
        storage.write(key, new byte[]{1,2,3});
        assertArrayEquals(new byte[]{1,2,3}, storage.read(key));
        assertArrayEquals(new byte[]{1,2,3}, storage.read(directory.resolve("photos").resolve(key).toString()));
        storage.delete(key);
        storage.delete(key);
        assertThrows(StorageException.class, () -> storage.read(key));
    }

    @Test
    void rejectsTraversalAndNeverOverwritesExistingFiles() throws Exception {
        var storage = new LocalFileStorageService(directory.resolve("photos").toString());
        Files.writeString(directory.resolve("outside.png"), "preserve");
        assertThrows(StorageException.class, () -> storage.read("../outside.png"));
        assertThrows(StorageException.class, () -> storage.delete(directory.resolve("outside.png").toString()));
        assertEquals("preserve", Files.readString(directory.resolve("outside.png")));
        storage.write("one.png", new byte[]{1});
        assertThrows(StorageException.class, () -> storage.write("one.png", new byte[]{2}));
        assertArrayEquals(new byte[]{1}, storage.read("one.png"));
    }

    @Test
    void existingPhotosAboveNewUploadLimitRemainReadable() throws Exception {
        Path root = directory.resolve("photos");
        var storage = new LocalFileStorageService(root.toString());
        Files.write(root.resolve("legacy.png"), new byte[PhotoValidator.MAX_BYTES + 1]);
        assertEquals(PhotoValidator.MAX_BYTES + 1, storage.read("legacy.png").length);
    }
}
