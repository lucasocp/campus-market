package br.edu.campusmarket.leiloes.application;
import br.edu.campusmarket.anuncios.application.ListingService;
import br.edu.campusmarket.anuncios.infrastructure.ListingRepository;
import br.edu.campusmarket.common.*;
import br.edu.campusmarket.leiloes.domain.Bid;
import br.edu.campusmarket.leiloes.infrastructure.BidRepository;
import br.edu.campusmarket.usuarios.domain.AppUser;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class BidService {
    private final ListingRepository listings;
    private final BidRepository bids;
    private final CatalogEvents events;
    private final UserNotifier notifications;
    private final Clock clock;
    private final ListingService views;
    public BidService(ListingRepository listings, BidRepository bids, CatalogEvents events,
                      UserNotifier notifications, Clock clock, ListingService views) {
        this.listings = listings; this.bids = bids; this.events = events;
        this.notifications = notifications; this.clock = clock; this.views = views;
    }
    public record BidView(UUID id, String bidderName, BigDecimal amount, Instant createdAt) { }
    public record Participation(ListingService.View listing, BigDecimal myHighestBid, boolean leading) { }
    @Transactional(readOnly = true) public List<Participation> mine(AppUser user) {
        var highest = new LinkedHashMap<UUID, Bid>();
        for (var bid : bids.findByBidderIdOrderByCreatedAtDesc(user.id())) {
            highest.merge(bid.listing().id(), bid, (a, b) -> a.amount().compareTo(b.amount()) >= 0 ? a : b);
        }
        return highest.values().stream().map(b -> new Participation(views.view(b.listing(), Set.of()),
            b.amount(), user.id().equals(b.listing().winnerId()))).toList();
    }
    public ListingService.View place(AppUser user, UUID listingId, BigDecimal amount) {
        // O bloqueio é adquirido antes de ler o maior lance e mantido até o commit.
        var listing = listings.lockById(listingId).orElseThrow(BusinessException::notFound);
        listing.placeBid(user.id(), amount, clock.instant());
        bids.save(new Bid(listing, user, amount, clock.instant()));
        notifications.notify(listing.seller(), "Novo lance de R$ " + amount + " em “" + listing.title() + "”.");
        events.changed(listingId);
        return views.view(listing, Set.of());
    }
    @Transactional(readOnly = true) public List<BidView> history(UUID listingId) {
        listings.findById(listingId).orElseThrow(BusinessException::notFound);
        return bids.findTop30ByListingIdOrderByAmountDesc(listingId).stream()
            .map(b -> new BidView(b.id(), "Estudante · " + b.bidder().id().toString().substring(0, 4), b.amount(), b.createdAt())).toList();
    }
}
