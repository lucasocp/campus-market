package br.edu.campusmarket.mensagens.domain;
import br.edu.campusmarket.anuncios.domain.Listing;
import br.edu.campusmarket.usuarios.domain.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "conversations", uniqueConstraints = @UniqueConstraint(columnNames = {"listing_id", "buyer_id"}))
public class Conversation {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "listing_id") private Listing listing;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "buyer_id") private AppUser buyer;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected Conversation() { }
    public Conversation(Listing listing, AppUser buyer, Instant now) { id = UUID.randomUUID(); this.listing = listing; this.buyer = buyer; updatedAt = now; }
    public boolean participant(UUID user) { return buyer.id().equals(user) || listing.seller().id().equals(user); }
    public AppUser other(UUID user) { return buyer.id().equals(user) ? listing.seller() : buyer; }
    public void touch(Instant now) { updatedAt = now; }
    public UUID id() { return id; }
    public Listing listing() { return listing; }
    public AppUser buyer() { return buyer; }
    public Instant updatedAt() { return updatedAt; }
}
