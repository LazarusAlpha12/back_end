package order.dto;
import java.util.Map;
public class ReporteDTO {
    private long totalPedidos;
    private Map<String, Long> porEstado;
    public ReporteDTO() {}
    public ReporteDTO(long totalPedidos, Map<String, Long> porEstado) { this.totalPedidos = totalPedidos; this.porEstado = porEstado; }
    public long getTotalPedidos() { return totalPedidos; }
    public void setTotalPedidos(long totalPedidos) { this.totalPedidos = totalPedidos; }
    public Map<String, Long> getPorEstado() { return porEstado; }
    public void setPorEstado(Map<String, Long> porEstado) { this.porEstado = porEstado; }
}
