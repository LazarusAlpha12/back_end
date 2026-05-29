package auth.dto;

import auth.entity.Rol;

/**
 * DTO de entrada para el endpoint POST /auth/register.
 *
 * Campos opcionales según el rol:
 *   REPARTIDOR          → capacidad (obligatorio), disponibilidad (opcional, default true)
 *   OPERADOR_LOGISTICO  → adminId (obligatorio)
 */
public record RegisterRequest(
        String nombre,
        String apellido,
        String email,
        String password,
        Rol rol,
        Integer capacidad,       // solo REPARTIDOR
        Boolean disponibilidad,  // solo REPARTIDOR (null = true por defecto)
        Long adminId             // solo OPERADOR_LOGISTICO
) {}
