package auth.repository;

import auth.entity.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {

    List<Repartidor> findByDisponibilidadTrue();
}
