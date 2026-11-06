package br.edu.campusmarket.negociacoes.domain;
import br.edu.campusmarket.anuncios.domain.Listing;
import br.edu.campusmarket.usuarios.domain.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static br.edu.campusmarket.common.BusinessException.require;

@Entity @Table(name = "offers")
public class Offer {
    public enum Status { PENDENTE, ACEITA, RECUSADA }
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "listing_id") private Listing listing;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "buyer_id") private AppUser buyer;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 1000) private String message;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected Offer() { }
    public Offer(Listing listing, AppUser buyer, BigDecimal amount, String message, Instant now) {
        require(listing.mode() == Listing.Mode.DIRETA && listing.status() == Listing.Status.ATIVO, "Produto indisponível para propostas.");
        require(!listing.seller().id().equals(buyer.id()), "Você não pode fazer uma proposta no próprio anúncio.");
        require(amount != null && amount.signum() > 0 && amount.compareTo(new BigDecimal("99999999.99")) <= 0
            && amount.stripTrailingZeros().scale() <= 2, "Valor de proposta inválido.");
        require(message != null && message.length() <= 1000, "Mensagem deve ter até 1000 caracteres.");
        id = UUID.randomUUID(); this.listing = listing; this.buyer = buyer; this.amount = amount;
        this.message = message.trim(); status = Status.PENDENTE; createdAt = now;
    }
    public void accept() { require(status == Status.PENDENTE, "Proposta já respondida."); status = Status.ACEITA; }
    public void decline() { require(status == Status.PENDENTE, "Proposta já respondida."); status = Status.RECUSADA; }
    public UUID id() { return id; }
    public Listing listing() { return listing; }
    public AppUser buyer() { return buyer; }
    public BigDecimal amount() { return amount; }
    public String message() { return message; }
    public Status status() { return status; }
    public Instant createdAt() { return createdAt; }
}
