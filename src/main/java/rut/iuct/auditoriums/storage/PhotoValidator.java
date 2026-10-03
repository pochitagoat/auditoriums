package rut.iuct.auditoriums.storage;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import rut.iuct.auditoriums.exception.ApiException;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Component
public class PhotoValidator {
    public static final int MAX_BYTES = 10_000_000;

    public byte[] validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw invalid(HttpStatus.BAD_REQUEST, "Выберите непустой файл");
        if (file.getSize() > MAX_BYTES) throw invalid(HttpStatus.PAYLOAD_TOO_LARGE, "Размер фотографии не должен превышать 10 000 000 байт");
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank() || name.length() > 255) {
            throw invalid(HttpStatus.BAD_REQUEST, "Имя файла должно содержать от 1 до 255 символов");
        }
        String lower = name.toLowerCase(Locale.ROOT);
        String expected = lower.endsWith(".png") ? "image/png"
                : lower.endsWith(".jpg") || lower.endsWith(".jpeg") ? "image/jpeg" : null;
        if (expected == null || !expected.equals(file.getContentType())) {
            throw invalid(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Допустимы только JPG, JPEG и PNG с соответствующим типом содержимого");
        }
        try (var stream = file.getInputStream()) {
            byte[] content = stream.readNBytes(MAX_BYTES + 1);
            if (content.length > MAX_BYTES) throw invalid(HttpStatus.PAYLOAD_TOO_LARGE, "Файл слишком большой");
            if (!expected.equals(contentType(content)) || ImageIO.read(new ByteArrayInputStream(content)) == null) {
                throw invalid(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Содержимое файла не соответствует формату изображения");
            }
            return content;
        } catch (IOException exception) {
            throw invalid(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Не удалось прочитать изображение");
        }
    }

    public static String contentType(byte[] content) {
        if (content.length >= 8 && content[0] == (byte) 0x89 && content[1] == 'P' && content[2] == 'N'
                && content[3] == 'G' && content[4] == 13 && content[5] == 10 && content[6] == 26 && content[7] == 10) return "image/png";
        if (content.length >= 3 && content[0] == (byte) 0xff && content[1] == (byte) 0xd8 && content[2] == (byte) 0xff) return "image/jpeg";
        return null;
    }

    private ApiException invalid(HttpStatus status, String message) {
        return new ApiException(status, message, Map.of("file", message));
    }
}
