package order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import order.entity.Ubicacion;

@Repository
public interface UbicacionRepositorio extends JpaRepository<Ubicacion, Long>
{
    Optional<Ubicacion> findByDireccion(String direccion);
}