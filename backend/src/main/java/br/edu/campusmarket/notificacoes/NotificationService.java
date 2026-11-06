package br.edu.campusmarket.notificacoes;
import br.edu.campusmarket.common.UserNotifier;
import br.edu.campusmarket.usuarios.domain.AppUser;
import br.edu.campusmarket.configuracao.RealtimeConfig.PrivateUpdate;
import java.time.Clock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class NotificationService implements UserNotifier {
    private final NotificationRepository repository; private final ApplicationEventPublisher events; private final Clock clock;
    public NotificationService(NotificationRepository repository, ApplicationEventPublisher events, Clock clock) { this.repository = repository; this.events = events; this.clock = clock; }
    @Transactional public void notify(AppUser recipient, String message) {
        repository.save(new Notification(recipient, message, clock.instant()));
        events.publishEvent(new PrivateUpdate(recipient.ra()));
    }
}
