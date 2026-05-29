package auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {

    @Id
    @Column(length = 36)
    private String jti;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public TokenBlacklist() {}

    public TokenBlacklist(String jti, LocalDateTime expiresAt) {
        this.jti = jti;
        this.expiresAt = expiresAt;
    }

    public String getJti() { return jti; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
