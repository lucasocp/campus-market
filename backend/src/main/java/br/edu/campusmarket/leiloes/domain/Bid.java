package br.edu.campusmarket.leiloes.domain;
import br.edu.campusmarket.anuncios.domain.Listing;
import br.edu.campusmarket.usuarios.domain.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "bids")
public class Bid {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "listing_id") private Listing listing;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "bidder_id") private AppUser bidder;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected Bid() { }
    public Bid(Listing listing, AppUser bidder, BigDecimal amount, Instant now) {
        id = UUID.randomUUID(); this.listing = listing; this.bidder = bidder; this.amount = amount; createdAt = now;
    }
    public UUID id() { return id; }
    public Listing listing() { return listing; }
    public AppUser bidder() { return bidder; }
    public BigDecimal amount() { return amount; }
    public Instant createdAt() { return createdAt; }
}
