package br.edu.campusmarket.anuncios.domain;
import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "listing_id"}))
public class Favorite {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "listing_id", nullable = false) private UUID listingId;
    protected Favorite() { }
    public Favorite(UUID userId, UUID listingId) { id = UUID.randomUUID(); this.userId = userId; this.listingId = listingId; }
    public UUID listingId() { return listingId; }
}
