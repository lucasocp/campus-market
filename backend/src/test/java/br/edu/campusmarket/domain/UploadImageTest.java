package br.edu.campusmarket.domain;
import br.edu.campusmarket.anuncios.application.UploadImage;
import br.edu.campusmarket.anuncios.domain.ImageStorage;
import br.edu.campusmarket.common.BusinessException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UploadImageTest {
    static class MemoryImages implements ImageStorage {
        byte[] bytes;
        public String save(byte[] jpeg) { bytes = jpeg; return "/api/images/test.jpg"; }
        public byte[] load(String key) { return bytes; }
    }
    @Test void invalidContentNeverReachesStorage() {
        var storage = new MemoryImages();
        assertThrows(BusinessException.class, () -> new UploadImage(storage).execute("not-an-image".getBytes()));
        assertNull(storage.bytes);
    }
    @Test void oversizedContentIsRejected() {
        assertThrows(BusinessException.class, () -> new UploadImage(new MemoryImages()).execute(new byte[5 * 1024 * 1024 + 1]));
    }
    @Test void pngIsNormalizedToJpegBeforeStorage() throws Exception {
        var png = new ByteArrayOutputStream(); ImageIO.write(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), "png", png);
        var storage = new MemoryImages();
        assertEquals("/api/images/test.jpg", new UploadImage(storage).execute(png.toByteArray()));
        assertEquals(0xff, storage.bytes[0] & 0xff); assertEquals(0xd8, storage.bytes[1] & 0xff);
    }
}
