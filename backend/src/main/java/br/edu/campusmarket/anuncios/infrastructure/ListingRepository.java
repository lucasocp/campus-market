package br.edu.campusmarket.anuncios.infrastructure;

import br.edu.campusmarket.anuncios.domain.Listing;
import java.time.Instant;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ListingRepository extends JpaRepository<Listing, UUID>, JpaSpecificationExecutor<Listing> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Listing l where l.id = :id")
    Optional<Listing> lockById(@Param("id") UUID id);
    List<Listing> findBySellerIdAndStatusNotOrderByCreatedAtDesc(UUID sellerId, Listing.Status status);
    List<Listing> findByModeAndStatusAndEndsAtLessThanEqual(Listing.Mode mode, Listing.Status status, Instant now);
}
