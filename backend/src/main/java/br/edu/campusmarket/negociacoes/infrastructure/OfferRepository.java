package br.edu.campusmarket.negociacoes.infrastructure;
import br.edu.campusmarket.negociacoes.domain.Offer;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface OfferRepository extends JpaRepository<Offer, UUID> {
    @Query("select o.listing.id from Offer o where o.id = :id")
    Optional<UUID> listingIdOf(@Param("id") UUID id);
    boolean existsByListingIdAndBuyerIdAndStatus(UUID listingId, UUID buyerId, Offer.Status status);
    List<Offer> findByListingIdAndStatus(UUID listingId, Offer.Status status);
    @Query("select o from Offer o where o.buyer.id = :user or o.listing.seller.id = :user order by o.createdAt desc")
    List<Offer> forUser(@Param("user") UUID user);
}
