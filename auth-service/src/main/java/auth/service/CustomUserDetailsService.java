package auth.service;

import auth.entity.Persona;
import auth.repository.PersonaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Integración de Persona con Spring Security.
 * Spring Security llama a este servicio durante la autenticación para cargar
 * el usuario desde la base de datos usando el email como "username".
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PersonaRepository personaRepository;

    public CustomUserDetailsService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Persona persona = personaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email));

        return new User(
                persona.getEmail(),
                persona.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + persona.getRol().name()))
        );
    }
}
