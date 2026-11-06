package br.edu.campusmarket.negociacoes.api;
import br.edu.campusmarket.common.CurrentUser;
import br.edu.campusmarket.negociacoes.application.NegotiationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class NegotiationController {
    private final NegotiationService service; private final CurrentUser current;
    public NegotiationController(NegotiationService service, CurrentUser current) { this.service = service; this.current = current; }
    public record OfferInput(@NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal amount,
        @NotNull @Size(max = 1000) String message) { }
    public record ResponseInput(boolean accept) { }
    @PostMapping("/listings/{id}/offers") NegotiationService.OfferView offer(@PathVariable UUID id, @Valid @RequestBody OfferInput input) { return service.offer(current.get(), id, input.amount(), input.message()); }
    @GetMapping("/offers") List<NegotiationService.OfferView> offers() { return service.offers(current.get()); }
    @PostMapping("/offers/{id}/response") void respond(@PathVariable UUID id, @RequestBody ResponseInput input) { service.respond(current.get(), id, input.accept()); }
    @GetMapping("/trades") List<NegotiationService.TradeView> trades() { return service.trades(current.get()); }
    @PostMapping("/trades/{id}/confirm") NegotiationService.TradeView confirm(@PathVariable UUID id) { return service.confirm(current.get(), id); }
}
