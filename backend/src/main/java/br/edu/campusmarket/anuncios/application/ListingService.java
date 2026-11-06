package br.edu.campusmarket.anuncios.application;

import br.edu.campusmarket.anuncios.domain.*;
import br.edu.campusmarket.anuncios.infrastructure.*;
import br.edu.campusmarket.common.*;
import br.edu.campusmarket.usuarios.domain.AppUser;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static br.edu.campusmarket.common.BusinessException.*;

@Service
@Transactional
public class ListingService {
    private final ListingRepository listings;
    private final FavoriteRepository favorites;
    private final CatalogEvents events;
    private final Clock clock;
    public ListingService(ListingRepository listings, FavoriteRepository favorites, CatalogEvents events, Clock clock) {
        this.listings = listings; this.favorites = favorites; this.events = events; this.clock = clock;
    }
    public record Input(String title, String description, String category, Listing.Condition condition,
        Listing.Mode mode, BigDecimal price, Instant endsAt, List<String> images) { }
    public record View(UUID id, UUID sellerId, String sellerName, String sellerCourse, String title,
        String description, String category, Listing.Condition condition, Listing.Mode mode, Listing.Status status,
        BigDecimal price, BigDecimal highestBid, BigDecimal minimumBid, int bidCount, Instant endsAt,
        Instant createdAt, List<String> images, boolean favorite) { }
    public record Catalog(List<View> items, long total, int pages, int page) { }
    public View view(Listing l, Set<UUID> starred) {
        return new View(l.id(), l.seller().id(), l.seller().name(), l.seller().course(), l.title(), l.description(),
            l.category(), l.condition(), l.mode(), l.status(), l.price(), l.highestBid(), l.minimumBid(),
            l.bidCount(), l.endsAt(), l.createdAt(), l.images(), starred.contains(l.id()));
    }
    private Set<UUID> starred(AppUser user) {
        return new HashSet<>(favorites.findByUserId(user.id()).stream().map(Favorite::listingId).toList());
    }
    @Transactional(readOnly = true)
    public Catalog catalog(AppUser user, String q, String category, String condition, String mode,
                           BigDecimal min, BigDecimal max, String order, int page, boolean onlyFavorites) {
        Set<UUID> starred = starred(user);
        Specification<Listing> spec = (root, query, cb) -> {
            var filters = new ArrayList<jakarta.persistence.criteria.Predicate>();
            filters.add(cb.equal(root.get("status"), Listing.Status.ATIVO));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
                filters.add(cb.or(cb.like(cb.lower(root.get("title")), term, '!'), cb.like(cb.lower(root.get("description")), term, '!')));
            }
            if (category != null && !category.isBlank()) filters.add(cb.equal(root.get("category"), category));
            if (condition != null && !condition.isBlank()) filters.add(cb.equal(root.get("condition"), Listing.Condition.valueOf(condition)));
            if (mode != null && !mode.isBlank()) filters.add(cb.equal(root.get("mode"), Listing.Mode.valueOf(mode)));
            if (min != null) filters.add(cb.ge(root.get("price"), min));
            if (max != null) filters.add(cb.le(root.get("price"), max));
            if (onlyFavorites) filters.add(starred.isEmpty() ? cb.disjunction() : root.get("id").in(starred));
            return cb.and(filters.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Sort sort = switch (order == null ? "recent" : order) {
            case "price-asc" -> Sort.by("price").ascending();
            case "price-desc" -> Sort.by("price").descending();
            case "ending" -> Sort.by(Sort.Order.asc("endsAt").nullsLast());
            default -> Sort.by("createdAt").descending();
        };
        var result = listings.findAll(spec, PageRequest.of(Math.max(0, page), 24, sort));
        return new Catalog(result.getContent().stream().map(l -> view(l, starred)).toList(), result.getTotalElements(), result.getTotalPages(), result.getNumber());
    }
    @Transactional(readOnly = true) public View get(AppUser user, UUID id) {
        Listing l = listings.findById(id).orElseThrow(BusinessException::notFound);
        if (l.status() == Listing.Status.EXCLUIDO) throw BusinessException.notFound();
        return view(l, starred(user));
    }
    @Transactional(readOnly = true) public List<View> mine(AppUser user) {
        var stars = starred(user);
        return listings.findBySellerIdAndStatusNotOrderByCreatedAtDesc(user.id(), Listing.Status.EXCLUIDO).stream().map(l -> view(l, stars)).toList();
    }
    public View create(AppUser user, Input input) {
        Listing l = new Listing(user, input.title(), input.description(), input.category(), input.condition(),
            input.mode(), input.price(), input.endsAt(), input.images(), clock.instant());
        listings.save(l); events.changed(l.id()); return view(l, Set.of());
    }
    public View edit(AppUser user, UUID id, Input input) {
        Listing l = listings.lockById(id).orElseThrow(BusinessException::notFound);
        l.edit(user.id(), input.title(), input.description(), input.category(), input.condition(), input.price(), input.images());
        events.changed(id); return view(l, starred(user));
    }
    public void pause(AppUser user, UUID id) {
        Listing l = listings.lockById(id).orElseThrow(BusinessException::notFound);
        l.togglePause(user.id()); events.changed(id);
    }
    public void remove(AppUser user, UUID id) {
        Listing l = listings.lockById(id).orElseThrow(BusinessException::notFound);
        l.remove(user.id()); events.changed(id);
    }
    public boolean favorite(AppUser user, UUID id) {
        listings.lockById(id).orElseThrow(BusinessException::notFound);
        var existing = favorites.findByUserIdAndListingId(user.id(), id);
        if (existing.isPresent()) { favorites.delete(existing.get()); return false; }
        favorites.save(new Favorite(user.id(), id)); return true;
    }
}
