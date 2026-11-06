package br.edu.campusmarket.mensagens.api;
import br.edu.campusmarket.common.CurrentUser;
import br.edu.campusmarket.mensagens.application.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/conversations")
public class ChatController {
    private final ChatService service; private final CurrentUser current;
    public ChatController(ChatService service, CurrentUser current) { this.service = service; this.current = current; }
    public record Start(@NotNull UUID listingId) { }
    public record Send(@NotBlank @Size(max = 2000) String content) { }
    @GetMapping List<ChatService.ConversationView> inbox() { return service.inbox(current.get()); }
    @PostMapping ChatService.ConversationView start(@Valid @RequestBody Start input) { return service.start(current.get(), input.listingId()); }
    @GetMapping("/{id}/messages") List<ChatService.MessageView> history(@PathVariable UUID id) { return service.history(current.get(), id); }
    @PostMapping("/{id}/messages") ChatService.MessageView send(@PathVariable UUID id, @Valid @RequestBody Send input) { return service.send(current.get(), id, input.content()); }
}
