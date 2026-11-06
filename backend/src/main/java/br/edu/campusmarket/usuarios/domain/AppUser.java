package br.edu.campusmarket.usuarios.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import static br.edu.campusmarket.common.BusinessException.require;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 20) private String ra;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, length = 100) private String course;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(nullable = false, length = 500) private String biography;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AppUser() { }
    public AppUser(String ra, String name, String course, String passwordHash) {
        require(ra != null && ra.matches("[0-9]{5,20}"), "R.A. inválido.");
        require(name != null && !name.isBlank() && name.trim().length() <= 100, "Nome inválido.");
        require(course != null && !course.isBlank() && course.trim().length() <= 100, "Curso inválido.");
        this.id = UUID.randomUUID();
        this.ra = ra;
        this.name = name.trim();
        this.course = course.trim();
        this.passwordHash = passwordHash;
        this.biography = "";
        this.createdAt = Instant.now();
    }
    public void updateProfile(String name, String course, String biography) {
        require(name != null && !name.isBlank() && name.trim().length() <= 100, "Nome inválido.");
        require(course != null && !course.isBlank() && course.trim().length() <= 100, "Curso inválido.");
        require(biography != null && biography.length() <= 500, "A descrição deve ter até 500 caracteres.");
        this.name = name.trim(); this.course = course.trim(); this.biography = biography.trim();
    }
    public UUID id() { return id; }
    public String ra() { return ra; }
    public String name() { return name; }
    public String course() { return course; }
    public String passwordHash() { return passwordHash; }
    public String biography() { return biography; }
    public Instant createdAt() { return createdAt; }
}
