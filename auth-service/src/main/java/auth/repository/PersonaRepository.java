package auth.repository;

import auth.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para Persona.
 * auth-service solo necesita buscar por email (login) y verificar existencia (registro).
 */
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByEmail(String email);

    boolean existsByEmail(String email);
}
