package br.edu.campusmarket.avaliacoes;
import br.edu.campusmarket.common.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
@RestController
public class ReviewController {
    private final ReviewService service; private final CurrentUser current;
    public ReviewController(ReviewService service, CurrentUser current) {
        this.service = service; this.current = current;
    }
    public record Input(@Min(1) @Max(5) int rating, @NotBlank @Size(min = 3, max = 1500) String comment) { }
    @PostMapping("/api/trades/{id}/review")
    void review(@PathVariable UUID id, @Valid @RequestBody Input input) {
        service.publish(current.get(), id, input.rating(), input.comment());
    }
}
