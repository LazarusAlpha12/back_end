package auth.scheduled;

import auth.repository.TokenBlacklistRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class TokenCleanupTask {

    private final TokenBlacklistRepository blacklistRepository;

    public TokenCleanupTask(TokenBlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    @Scheduled(fixedRate = 3_600_000) // cada hora
    @Transactional
    public void cleanExpiredTokens() {
        blacklistRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
