package user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import user.entity.Persona;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByEmail(String email);

    boolean existsByEmail(String email);
}
