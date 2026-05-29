package order.entity;

public enum EstadoPedido {
        PENDIENTE,
        ASIGNADO,
        EN_TRANSITO,
        ENTREGADO,
        CANCELADO;

    /**
     * Devuelve el string que el frontend espera recibir leer 
     * Mapeo explícito para evitar discrepancias silenciosas.
     */
    public String toFrontendValue() {
        return switch (this) {
            case PENDIENTE -> "Pendiente";
            case ASIGNADO -> "Asignado";
            case ENTREGADO -> "Entregado";
            case EN_TRANSITO -> "En_Transito";
            case CANCELADO -> "Cancelado";
        };
    }
}