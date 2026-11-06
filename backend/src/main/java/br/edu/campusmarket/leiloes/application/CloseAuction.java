package br.edu.campusmarket.leiloes.application;
import br.edu.campusmarket.anuncios.infrastructure.ListingRepository;
import br.edu.campusmarket.common.*;
import br.edu.campusmarket.negociacoes.domain.Trade;
import br.edu.campusmarket.negociacoes.infrastructure.TradeRepository;
import br.edu.campusmarket.usuarios.infrastructure.UserJpaRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CloseAuction {
    private final ListingRepository listings; private final TradeRepository trades;
    private final UserJpaRepository users; private final UserNotifier notifications;
    private final CatalogEvents events; private final Clock clock;
    public CloseAuction(ListingRepository listings, TradeRepository trades, UserJpaRepository users,
                        UserNotifier notifications, CatalogEvents events, Clock clock) {
        this.listings = listings; this.trades = trades; this.users = users;
        this.notifications = notifications; this.events = events; this.clock = clock;
    }
    @Transactional public void execute(UUID id) {
        var listing = listings.lockById(id).orElseThrow(BusinessException::notFound);
        if (!listing.closeAuction(clock.instant())) return;
        if (listing.winnerId() != null) {
            var winner = users.findById(listing.winnerId()).orElseThrow(BusinessException::notFound);
            trades.save(new Trade(listing, winner, listing.highestBid(), clock.instant()));
            notifications.notify(winner, "Você venceu o leilão de “" + listing.title() + "”! Combine a entrega pelo chat.");
            notifications.notify(listing.seller(), "Leilão encerrado: “" + listing.title() + "” tem um vencedor.");
        }
        events.changed(id);
    }
}
