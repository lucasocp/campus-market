package br.edu.campusmarket.domain;

import br.edu.campusmarket.anuncios.domain.Listing;
import br.edu.campusmarket.common.BusinessException;
import br.edu.campusmarket.negociacoes.domain.Trade;
import br.edu.campusmarket.avaliacoes.Review;
import br.edu.campusmarket.usuarios.domain.AppUser;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ListingRulesTest {
    private final Instant now = Instant.parse("2026-11-06T15:00:00Z");
    private final AppUser seller = new AppUser("20260001", "Vendedor", "Computação", "hash");
    private final AppUser buyer = new AppUser("20260002", "Comprador", "Computação", "hash");
    private Listing listing(Listing.Mode mode) {
        return new Listing(seller, "Livro de Java", "Livro completo e conservado.", "Livros", Listing.Condition.USADO,
            mode, new BigDecimal("100.00"), mode == Listing.Mode.LEILAO ? now.plusSeconds(3600) : null,
            List.of("/products/books.svg"), now);
    }
    @Test void firstBidMayEqualStartingPrice() {
        var auction = listing(Listing.Mode.LEILAO);
        auction.placeBid(buyer.id(), new BigDecimal("100.00"), now);
        assertEquals(1, auction.bidCount()); assertEquals(new BigDecimal("101.00"), auction.minimumBid());
    }
    @Test void lowerAndEqualBidsAreRejected() {
        var auction = listing(Listing.Mode.LEILAO);
        auction.placeBid(buyer.id(), new BigDecimal("100.00"), now);
        assertThrows(BusinessException.class, () -> auction.placeBid(UUID.randomUUID(), new BigDecimal("100.00"), now));
        assertEquals(1, auction.bidCount());
    }
    @Test void sellerCannotBid() {
        assertThrows(BusinessException.class, () -> listing(Listing.Mode.LEILAO).placeBid(seller.id(), new BigDecimal("100.00"), now));
    }
    @Test void bidAtExactDeadlineIsRejected() {
        var auction = listing(Listing.Mode.LEILAO);
        assertThrows(BusinessException.class, () -> auction.placeBid(buyer.id(), new BigDecimal("100.00"), auction.endsAt()));
    }
    @Test void directSaleDoesNotAcceptBids() {
        assertThrows(BusinessException.class, () -> listing(Listing.Mode.DIRETA).placeBid(buyer.id(), new BigDecimal("100.00"), now));
    }
    @Test void closeWithNoBidsHasNoWinner() {
        var auction = listing(Listing.Mode.LEILAO);
        assertTrue(auction.closeAuction(auction.endsAt()));
        assertEquals(Listing.Status.ENCERRADO, auction.status()); assertNull(auction.winnerId());
    }
    @Test void closeIsIdempotentAndReservesForWinner() {
        var auction = listing(Listing.Mode.LEILAO);
        auction.placeBid(buyer.id(), new BigDecimal("100.00"), now);
        assertTrue(auction.closeAuction(auction.endsAt())); assertFalse(auction.closeAuction(auction.endsAt()));
        assertEquals(Listing.Status.RESERVADO, auction.status()); assertEquals(buyer.id(), auction.winnerId());
    }
    @Test void auctionCannotCloseEarly() { assertFalse(listing(Listing.Mode.LEILAO).closeAuction(now)); }
    @Test void thirdPartyCannotPauseOrDelete() {
        var product = listing(Listing.Mode.DIRETA);
        assertThrows(BusinessException.class, () -> product.togglePause(buyer.id()));
        assertThrows(BusinessException.class, () -> product.remove(buyer.id()));
    }
    @Test void auctionWithBidsCannotBeEditedOrRemoved() {
        var auction = listing(Listing.Mode.LEILAO);
        auction.placeBid(buyer.id(), new BigDecimal("100.00"), now);
        assertThrows(BusinessException.class, () -> auction.edit(seller.id(), "Outro livro", "Outra descrição completa.", "Livros", Listing.Condition.NOVO, BigDecimal.ONE, List.of("/products/books.svg")));
        assertThrows(BusinessException.class, () -> auction.remove(seller.id()));
    }
    @Test void tradeRequiresBothConfirmationsBeforeReview() {
        var product = listing(Listing.Mode.DIRETA); product.reserve();
        var trade = new Trade(product, buyer, product.price(), now);
        assertThrows(BusinessException.class, () -> new Review(trade, buyer, 5, "Ótima experiência.", now));
        trade.confirm(buyer.id(), now);
        assertNull(trade.completedAt());
        trade.confirm(seller.id(), now);
        assertNotNull(trade.completedAt()); assertEquals(Listing.Status.VENDIDO, product.status());
        assertDoesNotThrow(() -> new Review(trade, buyer, 5, "Ótima experiência.", now));
    }
    @Test void outsiderCannotConfirmTrade() {
        var product = listing(Listing.Mode.DIRETA); product.reserve();
        var trade = new Trade(product, buyer, product.price(), now);
        assertThrows(BusinessException.class, () -> trade.confirm(UUID.randomUUID(), now));
    }
    @Test void fractionalCentsAreRejected() {
        assertThrows(BusinessException.class, () -> new Listing(seller, "Livro Java", "Descrição do livro.", "Livros", Listing.Condition.NOVO,
            Listing.Mode.DIRETA, new BigDecimal("10.001"), null, List.of("/products/books.svg"), now));
    }
}
