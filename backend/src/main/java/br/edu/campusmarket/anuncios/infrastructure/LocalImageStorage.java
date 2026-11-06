package br.edu.campusmarket.anuncios.infrastructure;
import br.edu.campusmarket.anuncios.domain.ImageStorage;
import br.edu.campusmarket.common.BusinessException;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class LocalImageStorage implements ImageStorage {
    private final Path directory;
    public LocalImageStorage(@Value("${campus.uploads}") String directory) { this.directory = Path.of(directory).toAbsolutePath().normalize(); }
    public String save(byte[] jpeg) {
        String key = UUID.randomUUID() + ".jpg";
        try { Files.createDirectories(directory); Files.write(directory.resolve(key), jpeg, StandardOpenOption.CREATE_NEW); return "/api/images/" + key; }
        catch (IOException ex) { throw new BusinessException(503, "Não foi possível armazenar a imagem."); }
    }
    public byte[] load(String key) {
        if (!key.matches("[a-f0-9-]{36}\\.jpg")) throw BusinessException.notFound();
        Path target = directory.resolve(key).normalize();
        if (!target.startsWith(directory)) throw BusinessException.notFound();
        try { return Files.readAllBytes(target); }
        catch (IOException ex) { throw BusinessException.notFound(); }
    }
}
