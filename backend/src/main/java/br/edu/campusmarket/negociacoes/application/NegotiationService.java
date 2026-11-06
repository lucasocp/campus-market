package br.edu.campusmarket.negociacoes.application;
import br.edu.campusmarket.anuncios.infrastructure.ListingRepository;
import br.edu.campusmarket.common.*;
import br.edu.campusmarket.negociacoes.domain.*;
import br.edu.campusmarket.negociacoes.infrastructure.*;
import br.edu.campusmarket.usuarios.domain.AppUser;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static br.edu.campusmarket.common.BusinessException.*;

@Service @Transactional
public class NegotiationService {
    private final ListingRepository listings; private final OfferRepository offers;
    private final TradeRepository trades; private final UserNotifier notifications;
    private final CatalogEvents events; private final Clock clock;
    public NegotiationService(ListingRepository listings, OfferRepository offers, TradeRepository trades,
                              UserNotifier notifications, CatalogEvents events, Clock clock) {
        this.listings = listings; this.offers = offers; this.trades = trades;
        this.notifications = notifications; this.events = events; this.clock = clock;
    }
    public record OfferView(UUID id, UUID listingId, String title, UUID sellerId, String sellerName, UUID buyerId,
        String buyerName, BigDecimal amount, String message, Offer.Status status, Instant createdAt) { }
    public record TradeView(UUID id, UUID listingId, String title, String image, UUID sellerId, String sellerName,
        UUID buyerId, String buyerName, BigDecimal amount, boolean buyerConfirmed, boolean sellerConfirmed,
        Instant completedAt) { }
    private OfferView view(Offer o) { return new OfferView(o.id(), o.listing().id(), o.listing().title(),
        o.listing().seller().id(), o.listing().seller().name(), o.buyer().id(), o.buyer().name(), o.amount(), o.message(), o.status(), o.createdAt()); }
    private TradeView view(Trade t) { return new TradeView(t.id(), t.listing().id(), t.listing().title(), t.listing().images().getFirst(),
        t.listing().seller().id(), t.listing().seller().name(), t.buyer().id(), t.buyer().name(), t.amount(), t.buyerConfirmed(), t.sellerConfirmed(), t.completedAt()); }
    public OfferView offer(AppUser buyer, UUID listingId, BigDecimal amount, String message) {
        var listing = listings.lockById(listingId).orElseThrow(BusinessException::notFound);
        require(!offers.existsByListingIdAndBuyerIdAndStatus(listingId, buyer.id(), Offer.Status.PENDENTE), "Você já tem uma proposta pendente neste anúncio.");
        var offer = offers.save(new Offer(listing, buyer, amount, message, clock.instant()));
        notifications.notify(listing.seller(), buyer.name() + " enviou uma proposta para “" + listing.title() + "”.");
        return view(offer);
    }
    public void respond(AppUser seller, UUID id, boolean accept) {
        // Descobre o agregado; depois adquire seu bloqueio e relê a proposta.
        UUID listingId = offers.listingIdOf(id).orElseThrow(BusinessException::notFound);
        var listing = listings.lockById(listingId).orElseThrow(BusinessException::notFound);
        var offer = offers.findById(id).orElseThrow(BusinessException::notFound);
        authorize(listing.seller().id().equals(seller.id()));
        if (accept) {
            listing.reserve(); offer.accept();
            trades.save(new Trade(listing, offer.buyer(), offer.amount(), clock.instant()));
            for (var other : offers.findByListingIdAndStatus(listingId, Offer.Status.PENDENTE)) {
                if (!other.id().equals(id)) other.decline();
            }
            events.changed(listingId);
        } else offer.decline();
        notifications.notify(offer.buyer(), "Sua proposta para “" + listing.title() + "” foi " + (accept ? "aceita. Combine a entrega pelo chat." : "recusada."));
    }
    @Transactional(readOnly = true) public List<OfferView> offers(AppUser user) { return offers.forUser(user.id()).stream().map(this::view).toList(); }
    @Transactional(readOnly = true) public List<TradeView> trades(AppUser user) { return trades.forUser(user.id()).stream().map(this::view).toList(); }
    public TradeView confirm(AppUser user, UUID id) {
        var trade = trades.lockById(id).orElseThrow(BusinessException::notFound);
        boolean wasCompleted = trade.completedAt() != null;
        trade.confirm(user.id(), clock.instant());
        if (!wasCompleted && trade.completedAt() != null) {
            events.changed(trade.listing().id());
            notifications.notify(trade.buyer(), "Negociação concluída! Você já pode avaliar “" + trade.listing().title() + "”.");
            notifications.notify(trade.listing().seller(), "Negociação concluída! Você já pode avaliar a experiência.");
        }
        return view(trade);
    }
}
