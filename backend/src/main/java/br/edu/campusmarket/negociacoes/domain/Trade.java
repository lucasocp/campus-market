package br.edu.campusmarket.negociacoes.domain;
import br.edu.campusmarket.anuncios.domain.Listing;
import br.edu.campusmarket.usuarios.domain.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static br.edu.campusmarket.common.BusinessException.*;

@Entity @Table(name = "trades")
public class Trade {
    @Id private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "listing_id", unique = true) private Listing listing;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "buyer_id") private AppUser buyer;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(name = "buyer_confirmed", nullable = false) private boolean buyerConfirmed;
    @Column(name = "seller_confirmed", nullable = false) private boolean sellerConfirmed;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "completed_at") private Instant completedAt;
    protected Trade() { }
    public Trade(Listing listing, AppUser buyer, BigDecimal amount, Instant now) {
        id = UUID.randomUUID(); this.listing = listing; this.buyer = buyer; this.amount = amount; createdAt = now;
    }
    public void confirm(UUID actor, Instant now) {
        authorize(participant(actor));
        if (completedAt != null) return;
        if (buyer.id().equals(actor)) buyerConfirmed = true;
        else sellerConfirmed = true;
        if (buyerConfirmed && sellerConfirmed) { listing.complete(); completedAt = now; }
    }
    public boolean participant(UUID actor) { return buyer.id().equals(actor) || listing.seller().id().equals(actor); }
    public UUID id() { return id; }
    public Listing listing() { return listing; }
    public AppUser buyer() { return buyer; }
    public BigDecimal amount() { return amount; }
    public boolean buyerConfirmed() { return buyerConfirmed; }
    public boolean sellerConfirmed() { return sellerConfirmed; }
    public Instant createdAt() { return createdAt; }
    public Instant completedAt() { return completedAt; }
}
