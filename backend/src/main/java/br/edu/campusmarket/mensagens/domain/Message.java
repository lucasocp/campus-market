package br.edu.campusmarket.mensagens.domain;
import br.edu.campusmarket.usuarios.domain.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import static br.edu.campusmarket.common.BusinessException.require;
@Entity @Table(name = "messages")
public class Message {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "conversation_id") private Conversation conversation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "sender_id") private AppUser sender;
    @Column(nullable = false, length = 2000) private String content;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected Message() { }
    public Message(Conversation conversation, AppUser sender, String content, Instant now) {
        require(content != null && !content.isBlank() && content.length() <= 2000, "A mensagem deve ter de 1 a 2000 caracteres.");
        id = UUID.randomUUID(); this.conversation = conversation; this.sender = sender; this.content = content.trim(); createdAt = now;
    }
    public UUID id() { return id; }
    public AppUser sender() { return sender; }
    public String content() { return content; }
    public Instant createdAt() { return createdAt; }
}
