package br.edu.campusmarket.anuncios.infrastructure;
import br.edu.campusmarket.anuncios.domain.Favorite;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserId(UUID userId);
    Optional<Favorite> findByUserIdAndListingId(UUID userId, UUID listingId);
}
