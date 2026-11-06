package br.edu.campusmarket.anuncios.api;
import br.edu.campusmarket.anuncios.application.UploadImage;
import br.edu.campusmarket.anuncios.domain.ImageStorage;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController @RequestMapping("/api/images")
public class ImageController {
    private final UploadImage upload; private final ImageStorage storage;
    public ImageController(UploadImage upload, ImageStorage storage) { this.upload = upload; this.storage = storage; }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) Map<String, String> upload(@RequestParam("image") MultipartFile image) throws IOException { return Map.of("url", upload.execute(image.getBytes())); }
    @GetMapping("/{key}") ResponseEntity<byte[]> get(@PathVariable String key) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).cacheControl(CacheControl.noStore()).body(storage.load(key));
    }
}
