package br.edu.campusmarket.notificacoes;
import br.edu.campusmarket.common.CurrentUser;
import java.time.Instant;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository repository; private final CurrentUser current;
    public NotificationController(NotificationRepository repository, CurrentUser current) { this.repository = repository; this.current = current; }
    public record View(UUID id, String content, boolean read, Instant createdAt) { }
    @GetMapping @Transactional(readOnly = true) List<View> list() { return repository.findTop50ByUserIdOrderByCreatedAtDesc(current.get().id()).stream().map(n -> new View(n.id(), n.content(), n.read(), n.createdAt())).toList(); }
    @PostMapping("/read") @Transactional void read() { repository.findByUserIdAndReadFalse(current.get().id()).forEach(Notification::markRead); }
}
