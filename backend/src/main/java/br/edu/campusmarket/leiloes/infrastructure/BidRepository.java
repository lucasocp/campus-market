package br.edu.campusmarket.leiloes.infrastructure;
import br.edu.campusmarket.leiloes.domain.Bid;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BidRepository extends JpaRepository<Bid, UUID> {
    List<Bid> findTop30ByListingIdOrderByAmountDesc(UUID listingId);
    List<Bid> findByBidderIdOrderByCreatedAtDesc(UUID bidderId);
}
