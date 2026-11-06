package br.edu.campusmarket.anuncios.application;
import br.edu.campusmarket.anuncios.domain.ImageStorage;
import br.edu.campusmarket.common.BusinessException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import static br.edu.campusmarket.common.BusinessException.require;

/** Verifica dimensões antes de decodificar e regrava JPEG sem metadados. */
@Service
public class UploadImage {
    private final ImageStorage storage;
    public UploadImage(ImageStorage storage) { this.storage = storage; }
    public String execute(byte[] bytes) {
        require(bytes.length > 0 && bytes.length <= 5 * 1024 * 1024, "A imagem deve ter até 5 MB.");
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            require(readers.hasNext(), "Envie uma imagem JPEG, PNG ou WebP compatível.");
            var reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                require(width > 0 && height > 0 && (long) width * height <= 16000000, "A imagem deve ter até 16 megapixels.");
                var source = reader.read(0);
                double ratio = Math.min(1.0, 1600.0 / Math.max(width, height));
                var result = new BufferedImage(Math.max(1, (int) (width * ratio)), Math.max(1, (int) (height * ratio)), BufferedImage.TYPE_INT_RGB);
                var graphics = result.createGraphics();
                try { graphics.setColor(Color.WHITE); graphics.fillRect(0, 0, result.getWidth(), result.getHeight()); graphics.drawImage(source, 0, 0, result.getWidth(), result.getHeight(), null); }
                finally { graphics.dispose(); }
                var output = new ByteArrayOutputStream();
                require(ImageIO.write(result, "jpg", output), "Não foi possível converter a imagem.");
                return storage.save(output.toByteArray());
            } finally { reader.dispose(); }
        } catch (IOException ex) { throw new BusinessException("Arquivo de imagem inválido."); }
    }
}
