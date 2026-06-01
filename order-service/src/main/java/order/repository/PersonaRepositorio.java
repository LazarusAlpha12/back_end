package order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import order.entity.Persona;

@Repository
public interface PersonaRepositorio extends JpaRepository<Persona, Long> {

    @Query("SELECT new map(p.id as id, p.nombre as nombre) FROM Persona p WHERE p.id IN :ids")
    List<Object[]> findNombresByIds(@Param("ids") List<Long> ids);

    @Query("SELECT p.rol from Persona p WHERE p.id = :id")
    String getRolPersona(@Param("id") Long id);
}
