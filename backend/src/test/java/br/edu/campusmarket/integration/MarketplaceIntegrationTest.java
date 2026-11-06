package br.edu.campusmarket.integration;

import br.edu.campusmarket.anuncios.application.ListingService;
import br.edu.campusmarket.anuncios.domain.Listing;
import br.edu.campusmarket.anuncios.infrastructure.ListingRepository;
import br.edu.campusmarket.common.BusinessException;
import br.edu.campusmarket.leiloes.application.*;
import br.edu.campusmarket.mensagens.application.ChatService;
import br.edu.campusmarket.negociacoes.application.NegotiationService;
import br.edu.campusmarket.negociacoes.infrastructure.TradeRepository;
import br.edu.campusmarket.usuarios.domain.AppUser;
import br.edu.campusmarket.usuarios.infrastructure.UserJpaRepository;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest @ActiveProfiles("test")
class MarketplaceIntegrationTest {
    @Autowired ListingService listings;
    @Autowired ListingRepository listingRepository;
    @Autowired BidService bids;
    @Autowired CloseAuction close;
    @Autowired UserJpaRepository users;
    @Autowired ChatService chat;
    @Autowired NegotiationService negotiations;
    @Autowired TradeRepository trades;
    @MockitoBean Clock clock;
    private static final AtomicInteger sequence = new AtomicInteger(10000000);
    private final Instant now = Instant.parse("2026-11-06T15:00:00Z");
    private AppUser seller, buyer, other;
    @BeforeEach void prepare() {
        when(clock.instant()).thenReturn(now);
        seller = user("Vendedor"); buyer = user("Comprador"); other = user("Outro estudante");
    }
    private AppUser user(String name) { return users.save(new AppUser(String.valueOf(sequence.incrementAndGet()), name, "Computação", "hash")); }
    private ListingService.View listing(Listing.Mode mode) {
        return listings.create(seller, new ListingService.Input("Livro para teste", "Livro conservado, com todas as páginas.", "Livros", Listing.Condition.USADO,
            mode, new BigDecimal("100.00"), mode == Listing.Mode.LEILAO ? now.plusSeconds(3600) : null, List.of("/products/books.svg")));
    }
    @Test void simultaneousEqualBidsProduceExactlyOneWinner() throws Exception {
        var listing = listing(Listing.Mode.LEILAO);
        var ready = new CountDownLatch(2); var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (var bidder : List.of(buyer, other)) futures.add(pool.submit(() -> {
                ready.countDown(); start.await();
                try { bids.place(bidder, listing.id(), new BigDecimal("100.00")); return true; }
                catch (BusinessException ex) { return false; }
            }));
            assertTrue(ready.await(10, TimeUnit.SECONDS)); start.countDown();
            int accepted = 0;
            for (var future : futures) if (future.get(15, TimeUnit.SECONDS)) accepted++;
            assertEquals(1, accepted);
        }
        var saved = listingRepository.findById(listing.id()).orElseThrow();
        assertEquals(1, saved.bidCount()); assertEquals(new BigDecimal("100.00"), saved.highestBid());
        assertEquals(1, bids.history(listing.id()).size());
    }
    @Test void repeatedAuctionClosingCreatesOnlyOneTrade() {
        var listing = listing(Listing.Mode.LEILAO);
        bids.place(buyer, listing.id(), new BigDecimal("110.00"));
        when(clock.instant()).thenReturn(now.plusSeconds(3601));
        close.execute(listing.id()); close.execute(listing.id());
        assertEquals(1, negotiations.trades(buyer).stream().filter(t -> t.listingId().equals(listing.id())).count());
        assertEquals(Listing.Status.RESERVADO, listingRepository.findById(listing.id()).orElseThrow().status());
    }
    @Test void outsiderCannotReadOrWriteConversation() {
        var listing = listing(Listing.Mode.DIRETA);
        var conversation = chat.start(buyer, listing.id());
        chat.send(buyer, conversation.id(), "Olá! Ainda está disponível?");
        assertThrows(BusinessException.class, () -> chat.history(other, conversation.id()));
        assertThrows(BusinessException.class, () -> chat.send(other, conversation.id(), "Mensagem invasora"));
        assertEquals(1, chat.history(seller, conversation.id()).size());
    }
    @Test void acceptedOfferAndTwoConfirmationsCompleteTrade() {
        var listing = listing(Listing.Mode.DIRETA);
        var offer = negotiations.offer(buyer, listing.id(), new BigDecimal("90.00"), "Retiro no campus.");
        assertThrows(BusinessException.class, () -> negotiations.respond(other, offer.id(), true));
        negotiations.respond(seller, offer.id(), true);
        var trade = negotiations.trades(buyer).getFirst();
        assertNull(negotiations.confirm(buyer, trade.id()).completedAt());
        assertNotNull(negotiations.confirm(seller, trade.id()).completedAt());
        assertEquals(Listing.Status.VENDIDO, listingRepository.findById(listing.id()).orElseThrow().status());
    }
    @Test void otherStudentCannotEditSellerListing() {
        var listing = listing(Listing.Mode.DIRETA);
        assertThrows(BusinessException.class, () -> listings.pause(other, listing.id()));
        assertEquals(Listing.Status.ATIVO, listingRepository.findById(listing.id()).orElseThrow().status());
    }
}
