package br.edu.campusmarket.leiloes.infrastructure;
import br.edu.campusmarket.anuncios.domain.Listing;
import br.edu.campusmarket.anuncios.infrastructure.ListingRepository;
import br.edu.campusmarket.leiloes.application.CloseAuction;
import java.time.Clock;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

/** O caso de uso está em outro bean para que a transação seja interceptada. */
@Component @Profile("!test")
public class AuctionScheduler {
    private static final Logger log = LoggerFactory.getLogger(AuctionScheduler.class);
    private final ListingRepository listings; private final CloseAuction close; private final Clock clock;
    public AuctionScheduler(ListingRepository listings, CloseAuction close, Clock clock) { this.listings = listings; this.close = close; this.clock = clock; }
    @Scheduled(fixedDelay = 5000, initialDelay = 5000) public void tick() {
        for (var listing : listings.findByModeAndStatusAndEndsAtLessThanEqual(Listing.Mode.LEILAO, Listing.Status.ATIVO, clock.instant())) {
            try { close.execute(listing.id()); }
            catch (RuntimeException ex) { log.error("Falha ao encerrar leilão {}. A rotina tentará novamente.", listing.id(), ex); }
        }
    }
}
