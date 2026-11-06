package br.edu.campusmarket.usuarios.api;
import br.edu.campusmarket.anuncios.application.ListingService;
import br.edu.campusmarket.anuncios.domain.Listing;
import br.edu.campusmarket.anuncios.infrastructure.ListingRepository;
import br.edu.campusmarket.avaliacoes.ReviewRepository;
import br.edu.campusmarket.common.*;
import br.edu.campusmarket.usuarios.infrastructure.UserJpaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/users")
public class ProfileController {
    private final UserJpaRepository users; private final ListingRepository listings;
    private final ReviewRepository reviews; private final ListingService views; private final CurrentUser current;
    public ProfileController(UserJpaRepository users, ListingRepository listings, ReviewRepository reviews, ListingService views, CurrentUser current) {
        this.users = users; this.listings = listings; this.reviews = reviews; this.views = views; this.current = current;
    }
    public record ReviewView(UUID id, String authorName, int rating, String comment, Instant createdAt) { }
    public record Profile(UUID id, String name, String course, String biography, Instant createdAt,
        double rating, int reviewCount, List<ReviewView> reviews, List<ListingService.View> listings) { }
    @GetMapping("/{id}") @Transactional(readOnly = true) Profile profile(@PathVariable UUID id) {
        var user = users.findById(id).orElseThrow(BusinessException::notFound);
        var feedback = reviews.findByRecipientIdOrderByCreatedAtDesc(id).stream()
            .map(r -> new ReviewView(r.id(), r.author().name(), r.rating(), r.comment(), r.createdAt())).toList();
        var products = listings.findBySellerIdAndStatusNotOrderByCreatedAtDesc(id, Listing.Status.EXCLUIDO).stream()
            .filter(l -> l.status() == Listing.Status.ATIVO).map(l -> views.view(l, Set.of())).toList();
        return new Profile(user.id(), user.name(), user.course(), user.biography(), user.createdAt(),
            feedback.stream().mapToInt(ReviewView::rating).average().orElse(0), feedback.size(), feedback, products);
    }
    public record Update(@NotBlank @Size(max = 100) String name, @NotBlank @Size(max = 100) String course,
        @NotNull @Size(max = 500) String biography) { }
    @PutMapping("/me") @Transactional AuthController.Me update(@Valid @RequestBody Update input) {
        var user = current.get(); user.updateProfile(input.name(), input.course(), input.biography());
        return AuthController.Me.of(user);
    }
}
