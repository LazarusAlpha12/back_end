package auth.config;

import auth.repository.TokenBlacklistRepository;
import auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistRepository blacklistRepository;

    public JwtAuthenticationFilter(JwtService jwtService, TokenBlacklistRepository blacklistRepository) {
        this.jwtService = jwtService;
        this.blacklistRepository = blacklistRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String jti = jwtService.extractJti(token);

            if (blacklistRepository.existsByJti(jti)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revocado");
                return;
            }

            String email = jwtService.extractEmail(token);

            if (email != null && jwtService.isTokenValid(token, email)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String rol = (String) jwtService.extractClaim(token, claims -> claims.get("rol"));

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
