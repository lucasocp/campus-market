package br.edu.campusmarket.mensagens.infrastructure;
import br.edu.campusmarket.mensagens.domain.Conversation;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByListingIdAndBuyerId(UUID listingId, UUID buyerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from Conversation c where c.id = :id")
    Optional<Conversation> lockById(@Param("id") UUID id);
    @Query("select c from Conversation c where c.buyer.id = :user or c.listing.seller.id = :user order by c.updatedAt desc")
    List<Conversation> forUser(@Param("user") UUID user);
}
