package order.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import order.service.JwtService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /*
    Uso esta clase para autenticar en el filtro
    la validez del token como el rol al que este compete
    */
    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Extraer email del token (sin necesidad de validar contra BD)
                String email = jwtService.extractEmail(token);
                if (email != null && jwtService.isTokenValid(token, email)) {
                    // Extraer rol del token (usando jwtService)
                    Long userId = jwtService.extractUserId(token);  // método nuevo
                    String rol = (String) jwtService.extractClaim(token, claims -> claims.get("rol"));
                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // Token inválido – no autenticar
            }
        }
        chain.doFilter(request, response);
    }
}