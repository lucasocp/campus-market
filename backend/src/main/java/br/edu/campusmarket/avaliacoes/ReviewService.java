package br.edu.campusmarket.avaliacoes;
import br.edu.campusmarket.common.BusinessException;
import br.edu.campusmarket.negociacoes.infrastructure.TradeRepository;
import br.edu.campusmarket.usuarios.domain.AppUser;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static br.edu.campusmarket.common.BusinessException.require;

@Service
public class ReviewService {
    private final TradeRepository trades; private final ReviewRepository reviews; private final Clock clock;
    public ReviewService(TradeRepository trades, ReviewRepository reviews, Clock clock) {
        this.trades = trades; this.reviews = reviews; this.clock = clock;
    }
    @Transactional public void publish(AppUser author, UUID tradeId, int rating, String comment) {
        var trade = trades.lockById(tradeId).orElseThrow(BusinessException::notFound);
        require(!reviews.existsByTradeIdAndAuthorId(tradeId, author.id()), "Você já avaliou esta negociação.");
        reviews.save(new Review(trade, author, rating, comment, clock.instant()));
    }
}
