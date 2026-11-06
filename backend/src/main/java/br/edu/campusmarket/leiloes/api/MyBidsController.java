package br.edu.campusmarket.leiloes.api;
import br.edu.campusmarket.common.CurrentUser;
import br.edu.campusmarket.leiloes.application.BidService;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController
public class MyBidsController {
    private final BidService service; private final CurrentUser current;
    public MyBidsController(BidService service, CurrentUser current) { this.service = service; this.current = current; }
    @GetMapping("/api/bids/mine") List<BidService.Participation> mine() { return service.mine(current.get()); }
}
