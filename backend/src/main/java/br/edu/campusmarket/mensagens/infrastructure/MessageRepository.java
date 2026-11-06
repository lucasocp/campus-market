package br.edu.campusmarket.mensagens.infrastructure;
import br.edu.campusmarket.mensagens.domain.Message;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findTop200ByConversationIdOrderByCreatedAtDesc(UUID id);
    Optional<Message> findTopByConversationIdOrderByCreatedAtDesc(UUID id);
}
