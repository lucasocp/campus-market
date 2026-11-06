package br.edu.campusmarket.negociacoes.infrastructure;
import br.edu.campusmarket.negociacoes.domain.Trade;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface TradeRepository extends JpaRepository<Trade, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from Trade t where t.id = :id")
    Optional<Trade> lockById(@Param("id") UUID id);
    @Query("select t from Trade t where t.buyer.id = :user or t.listing.seller.id = :user order by t.createdAt desc")
    List<Trade> forUser(@Param("user") UUID user);
}
