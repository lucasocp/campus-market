package br.edu.campusmarket.avaliacoes;
import br.edu.campusmarket.negociacoes.domain.Trade;
import br.edu.campusmarket.usuarios.domain.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import static br.edu.campusmarket.common.BusinessException.*;

@Entity @Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"trade_id", "author_id"}))
public class Review {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "trade_id") private Trade trade;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "author_id") private AppUser author;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "recipient_id") private AppUser recipient;
    @Column(nullable = false) private int rating;
    @Column(nullable = false, length = 1500) private String comment;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected Review() { }
    public Review(Trade trade, AppUser author, int rating, String comment, Instant now) {
        authorize(trade.participant(author.id()));
        require(trade.completedAt() != null, "A avaliação só é permitida depois da confirmação dos dois participantes.");
        require(rating >= 1 && rating <= 5, "A nota deve estar entre 1 e 5.");
        require(comment != null && comment.trim().length() >= 3 && comment.length() <= 1500, "Comentário deve ter de 3 a 1500 caracteres.");
        id = UUID.randomUUID(); this.trade = trade; this.author = author;
        recipient = trade.buyer().id().equals(author.id()) ? trade.listing().seller() : trade.buyer();
        this.rating = rating; this.comment = comment.trim(); createdAt = now;
    }
    public UUID id() { return id; }
    public AppUser author() { return author; }
    public int rating() { return rating; }
    public String comment() { return comment; }
    public Instant createdAt() { return createdAt; }
}
