package user.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import user.entity.Configuracion;
import user.repository.ConfiguracionRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ConfiguracionRepository configRepo;

    public DataInitializer(ConfiguracionRepository configRepo) {
        this.configRepo = configRepo;
    }

    @Override
    public void run(String... args) {
        if (configRepo.count() == 0) {
            configRepo.save(new Configuracion("MANTENIMIENTO", "Modo Mantenimiento", "false", "boolean"));
            configRepo.save(new Configuracion("MAX_PEDIDOS", "Máx. Pedidos/Día", "1000", "number"));
            configRepo.save(new Configuracion("CORREO_ADMIN", "Correo de Administrador", "admin@test.com", "text"));
        }
    }
}
