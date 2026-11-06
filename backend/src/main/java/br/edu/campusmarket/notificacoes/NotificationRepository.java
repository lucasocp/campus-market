package br.edu.campusmarket.notificacoes;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findTop50ByUserIdOrderByCreatedAtDesc(UUID id);
    List<Notification> findByUserIdAndReadFalse(UUID id);
}
