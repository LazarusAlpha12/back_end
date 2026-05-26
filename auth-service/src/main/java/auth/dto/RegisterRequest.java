package auth.dto;

import auth.entity.Rol;

/**
 * DTO de entrada para el endpoint POST /auth/register.
 */
public record RegisterRequest(
        String nombre,
        String apellido,
        String email,
        String password,
        Rol rol
) {}
