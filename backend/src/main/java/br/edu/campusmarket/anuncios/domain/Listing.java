package br.edu.campusmarket.anuncios.domain;

import br.edu.campusmarket.usuarios.domain.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static br.edu.campusmarket.common.BusinessException.*;

/** Agregado que protege as transições e os valores do anúncio. */
@Entity
@Table(name = "listings")
public class Listing {
    public enum Mode { DIRETA, LEILAO }
    public enum Condition { NOVO, SEMINOVO, USADO }
    public enum Status { ATIVO, PAUSADO, RESERVADO, VENDIDO, ENCERRADO, EXCLUIDO }
    public static final Set<String> CATEGORIES = Set.of("Livros", "Tecnologia", "Materiais", "Moda", "Outros");

    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "seller_id") private AppUser seller;
    @Column(nullable = false, length = 120) private String title;
    @Column(nullable = false, length = 4000) private String description;
    @Column(nullable = false, length = 30) private String category;
    @Enumerated(EnumType.STRING) @Column(name = "item_condition", nullable = false) private Condition condition;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Mode mode;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
    @Column(name = "highest_bid", nullable = false, precision = 12, scale = 2) private BigDecimal highestBid;
    @Column(name = "bid_count", nullable = false) private int bidCount;
    @Column(name = "winner_id") private UUID winnerId;
    @Column(name = "ends_at") private Instant endsAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @ElementCollection
    @CollectionTable(name = "listing_images", joinColumns = @JoinColumn(name = "listing_id"))
    @OrderColumn(name = "image_order") @Column(name = "image_url", nullable = false, length = 1000)
    private List<String> images = new ArrayList<>();

    protected Listing() { }
    public Listing(AppUser seller, String title, String description, String category, Condition condition,
                   Mode mode, BigDecimal price, Instant endsAt, List<String> images, Instant now) {
        require(seller != null && mode != null && condition != null, "Dados do anúncio incompletos.");
        this.id = UUID.randomUUID(); this.seller = seller; this.mode = mode;
        this.status = Status.ATIVO; this.highestBid = BigDecimal.ZERO; this.createdAt = now;
        if (mode == Mode.LEILAO) {
            require(endsAt != null && endsAt.isAfter(now.plusSeconds(59)) && !endsAt.isAfter(now.plusSeconds(604800)),
                "O leilão deve durar entre 1 minuto e 7 dias.");
            this.endsAt = endsAt;
        }
        setDetails(title, description, category, condition, price, images);
    }
    private void setDetails(String title, String description, String category, Condition condition,
                            BigDecimal price, List<String> images) {
        require(title != null && title.trim().length() >= 3 && title.trim().length() <= 120, "Título deve ter de 3 a 120 caracteres.");
        require(description != null && description.trim().length() >= 10 && description.length() <= 4000, "Descrição deve ter de 10 a 4000 caracteres.");
        require(category != null && CATEGORIES.contains(category), "Categoria inválida.");
        require(condition != null, "Informe o estado de conservação.");
        require(price != null && price.signum() > 0 && price.compareTo(new BigDecimal("99999999.99")) <= 0
            && price.stripTrailingZeros().scale() <= 2, "Informe um preço positivo com até duas casas decimais.");
        require(images != null && !images.isEmpty() && images.size() <= 5, "Adicione de 1 a 5 imagens.");
        require(images.stream().allMatch(s -> s != null && s.length() <= 1000
            && (s.matches("/api/images/[a-f0-9-]+\\.jpg") || s.matches("/products/[a-z-]+\\.svg"))), "Imagem inválida. Utilize o envio de imagens.");
        this.title = title.trim(); this.description = description.trim(); this.category = category;
        this.condition = condition; this.price = price.setScale(2);
        this.images.clear(); this.images.addAll(images);
    }
    public void edit(UUID actor, String title, String description, String category, Condition condition,
                     BigDecimal price, List<String> images) {
        authorize(seller.id().equals(actor));
        require(status == Status.ATIVO || status == Status.PAUSADO, "Este anúncio não pode ser editado.");
        require(bidCount == 0, "Não é possível editar um leilão que já recebeu lances.");
        setDetails(title, description, category, condition, price, images);
    }
    public void togglePause(UUID actor) {
        authorize(seller.id().equals(actor));
        require(mode == Mode.DIRETA, "Leilões não podem ser pausados; o prazo precisa ser preservado.");
        require(status == Status.ATIVO || status == Status.PAUSADO, "Este anúncio não pode ser pausado.");
        status = status == Status.ATIVO ? Status.PAUSADO : Status.ATIVO;
    }
    public void remove(UUID actor) {
        authorize(seller.id().equals(actor));
        require((status == Status.ATIVO || status == Status.PAUSADO || status == Status.ENCERRADO) && bidCount == 0,
            "Anúncios com lances ou negociações devem preservar seu histórico.");
        status = Status.EXCLUIDO;
    }
    public void placeBid(UUID bidder, BigDecimal amount, Instant now) {
        require(mode == Mode.LEILAO && status == Status.ATIVO, "Este leilão não está ativo.");
        require(now.isBefore(endsAt), "O prazo deste leilão terminou.");
        require(!seller.id().equals(bidder), "O vendedor não pode dar lance no próprio anúncio.");
        BigDecimal minimum = minimumBid();
        require(amount != null && amount.compareTo(minimum) >= 0 && amount.compareTo(new BigDecimal("99999999.99")) <= 0
            && amount.stripTrailingZeros().scale() <= 2, "O lance mínimo é R$ " + minimum.toPlainString() + ".");
        highestBid = amount.setScale(2); winnerId = bidder; bidCount++;
    }
    public boolean closeAuction(Instant now) {
        if (mode != Mode.LEILAO || status != Status.ATIVO || now.isBefore(endsAt)) return false;
        status = bidCount == 0 ? Status.ENCERRADO : Status.RESERVADO;
        return true;
    }
    public void reserve() {
        require(status == Status.ATIVO && mode == Mode.DIRETA, "O produto não está disponível para venda direta.");
        status = Status.RESERVADO;
    }
    public void complete() {
        require(status == Status.RESERVADO, "O produto não possui uma negociação em andamento.");
        status = Status.VENDIDO;
    }
    public BigDecimal minimumBid() { return bidCount == 0 ? price : highestBid.add(BigDecimal.ONE); }
    public UUID id() { return id; }
    public AppUser seller() { return seller; }
    public String title() { return title; }
    public String description() { return description; }
    public String category() { return category; }
    public Condition condition() { return condition; }
    public Mode mode() { return mode; }
    public Status status() { return status; }
    public BigDecimal price() { return price; }
    public BigDecimal highestBid() { return highestBid; }
    public int bidCount() { return bidCount; }
    public UUID winnerId() { return winnerId; }
    public Instant endsAt() { return endsAt; }
    public Instant createdAt() { return createdAt; }
    public List<String> images() { return List.copyOf(images); }
}
