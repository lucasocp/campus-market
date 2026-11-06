package br.edu.campusmarket.leiloes.api;
import br.edu.campusmarket.anuncios.application.ListingService;
import br.edu.campusmarket.common.CurrentUser;
import br.edu.campusmarket.leiloes.application.BidService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/listings/{id}/bids")
public class BidController {
    private final BidService service; private final CurrentUser current;
    public BidController(BidService service, CurrentUser current) { this.service = service; this.current = current; }
    public record Input(@NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal amount) { }
    @GetMapping List<BidService.BidView> history(@PathVariable UUID id) { return service.history(id); }
    @PostMapping ListingService.View place(@PathVariable UUID id, @Valid @RequestBody Input input) { return service.place(current.get(), id, input.amount()); }
}
