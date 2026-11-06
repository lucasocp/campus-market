package br.edu.campusmarket.notificacoes;
import br.edu.campusmarket.usuarios.domain.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name = "notifications")
public class Notification {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private AppUser user;
    @Column(nullable = false, length = 500) private String content;
    @Column(name = "is_read", nullable = false) private boolean read;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected Notification() { }
    public Notification(AppUser user, String content, Instant now) { id = UUID.randomUUID(); this.user = user; this.content = content; createdAt = now; }
    public void markRead() { read = true; }
    public UUID id() { return id; }
    public String content() { return content; }
    public boolean read() { return read; }
    public Instant createdAt() { return createdAt; }
}
