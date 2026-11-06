package br.edu.campusmarket.avaliacoes;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByTradeIdAndAuthorId(UUID tradeId, UUID authorId);
    List<Review> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
}
