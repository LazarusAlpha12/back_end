package auth.service;

import auth.entity.Persona;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * Responsabilidad única: generar y validar tokens JWT.
 *
 * Algoritmo: HMAC-SHA256 (simétrico).
 * La misma clave secreta se comparte con user-service y order-service
 * para que puedan validar el token localmente sin llamar a auth-service.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expirationMs;

    // ── Generación ─────────────────────────────────────────────────────────────

    /**
     * Genera un JWT firmado con HMAC-SHA256.
     * Claims incluidos: sub (email), id, nombre, rol (string del frontend), jti (UUID único).
     */
    public String generateToken(Persona persona) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti — permite revocación individual
                .subject(persona.getEmail())
                .claim("id", persona.getId())
                .claim("nombre", persona.getNombre())
                .claim("rol", persona.getRol().toFrontendValue()) // "Administrador", "OperadorLogistico", etc.
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Validación ─────────────────────────────────────────────────────────────

    /**
     * Valida que el token sea correcto y no esté expirado.
     */
    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return tokenEmail.equals(email) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // ── Extracción de Claims ───────────────────────────────────────────────────

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public LocalDateTime extractExpiration(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return Instant.ofEpochMilli(exp.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ── Clave ──────────────────────────────────────────────────────────────────

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
