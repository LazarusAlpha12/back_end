package auth.repository;

import auth.entity.OperadorLogistico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperadorLogisticoRepository extends JpaRepository<OperadorLogistico, Long> {

    List<OperadorLogistico> findByAdminId(Long adminId);
}
