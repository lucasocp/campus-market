package br.edu.campusmarket.anuncios.api;

import br.edu.campusmarket.anuncios.application.ListingService;
import br.edu.campusmarket.common.CurrentUser;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/listings")
public class ListingController {
    private final ListingService service;
    private final CurrentUser current;
    public ListingController(ListingService service, CurrentUser current) { this.service = service; this.current = current; }
    @GetMapping ListingService.Catalog catalog(@RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "") String category, @RequestParam(defaultValue = "") String condition,
        @RequestParam(defaultValue = "") String mode, @RequestParam(required = false) BigDecimal min,
        @RequestParam(required = false) BigDecimal max, @RequestParam(defaultValue = "recent") String order,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "false") boolean favorites) {
        return service.catalog(current.get(), q, category, condition, mode, min, max, order, page, favorites);
    }
    @GetMapping("/mine") List<ListingService.View> mine() { return service.mine(current.get()); }
    @GetMapping("/{id}") ListingService.View get(@PathVariable UUID id) { return service.get(current.get(), id); }
    @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    ListingService.View create(@RequestBody ListingService.Input input) { return service.create(current.get(), input); }
    @PutMapping("/{id}") ListingService.View edit(@PathVariable UUID id, @RequestBody ListingService.Input input) { return service.edit(current.get(), id, input); }
    @PostMapping("/{id}/pause") void pause(@PathVariable UUID id) { service.pause(current.get(), id); }
    @DeleteMapping("/{id}") void remove(@PathVariable UUID id) { service.remove(current.get(), id); }
    @PostMapping("/{id}/favorite") Map<String, Boolean> favorite(@PathVariable UUID id) { return Map.of("favorite", service.favorite(current.get(), id)); }
}
