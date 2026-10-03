package rut.iuct.auditoriums.storage;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import rut.iuct.auditoriums.exception.ApiException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import static org.junit.jupiter.api.Assertions.*;

public class PhotoValidatorTests {
    private final PhotoValidator validator = new PhotoValidator();

    public static byte[] image(String format) throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB), format, bytes);
        return bytes.toByteArray();
    }

    @Test
    void acceptsJpegAndPngWithCaseInsensitiveExtensions() throws Exception {
        for (String name : new String[]{"photo.JPG", "photo.JPEG", "photo.PNG"}) {
            boolean png = name.endsWith("PNG");
            byte[] content = image(png ? "png" : "jpeg");
            assertArrayEquals(content, validator.validate(new MockMultipartFile("file", name, png ? "image/png" : "image/jpeg", content)));
        }
    }

    @Test
    void rejectsWebpSpoofedContentMismatchAndEmptyFiles() throws Exception {
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, assertThrows(ApiException.class,
                () -> validator.validate(new MockMultipartFile("file", "photo.webp", "image/webp", image("png")))).getStatus());
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, assertThrows(ApiException.class,
                () -> validator.validate(new MockMultipartFile("file", "photo.png", "image/png", "not an image".getBytes()))).getStatus());
        assertThrows(ApiException.class, () -> validator.validate(new MockMultipartFile("file", "photo.jpg", "image/jpeg", image("png"))));
        assertThrows(ApiException.class, () -> validator.validate(new MockMultipartFile("file", "photo.png", "image/png", new byte[0])));
        assertThrows(ApiException.class, () -> validator.validate(new MockMultipartFile("file", "photo.png", "text/html", image("png"))));
    }

    @Test
    void acceptsExactLimitAndRejectsOneByteOverLimit() throws Exception {
        byte[] content = java.util.Arrays.copyOf(image("png"), PhotoValidator.MAX_BYTES);
        assertEquals(PhotoValidator.MAX_BYTES, validator.validate(new MockMultipartFile("file", "photo.png", "image/png", content)).length);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, assertThrows(ApiException.class,
                () -> validator.validate(new MockMultipartFile("file", "photo.png", "image/png", new byte[PhotoValidator.MAX_BYTES + 1]))).getStatus());
    }
}
