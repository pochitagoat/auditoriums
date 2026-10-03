package rut.iuct.auditoriums.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path root;

    public LocalFileStorageService(@Value("${app.storage.root}") String directory) {
        try {
            Path configured = Path.of(directory).toAbsolutePath().normalize();
            Files.createDirectories(configured);
            root = configured.toRealPath();
        } catch (IOException exception) {
            throw new StorageException("Не удалось открыть хранилище фотографий", exception);
        }
    }

    @Override
    public String newKey(String contentType) {
        return UUID.randomUUID() + ("image/png".equals(contentType) ? ".png" : ".jpg");
    }

    private Path resolve(String key) throws IOException {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root) || path.equals(root)) throw new IOException("Path outside storage");
        // Старые абсолютные пути допустимы только внутри настроенного корня.
        for (Path current = path; current != null && !current.equals(root); current = current.getParent()) {
            if (Files.isSymbolicLink(current)) throw new IOException("Symbolic links are not supported");
        }
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && !path.toRealPath().startsWith(root)) {
            throw new IOException("Path outside storage");
        }
        return path;
    }

    @Override
    public void write(String key, byte[] content) {
        try {
            Path path = resolve(key);
            Files.createDirectories(path.getParent());
            Files.write(path, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException | IllegalArgumentException exception) {
            throw new StorageException("Не удалось сохранить фотографию", exception);
        }
    }

    @Override
    public String canonicalKey(String key) {
        try { return root.relativize(resolve(key)).toString().replace('\\', '/'); }
        catch (IOException | IllegalArgumentException exception) {
            throw new StorageException("Недопустимый путь фотографии", exception);
        }
    }

    @Override
    public byte[] read(String key) {
        try {
            Path path = resolve(key);
            // Лимит относится к новым загрузкам; старые фотографии могут быть больше.
            return Files.readAllBytes(path);
        } catch (IOException | IllegalArgumentException exception) {
            throw new StorageException("Файл фотографии недоступен", exception);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException | IllegalArgumentException exception) {
            throw new StorageException("Не удалось удалить файл фотографии", exception);
        }
    }
}
