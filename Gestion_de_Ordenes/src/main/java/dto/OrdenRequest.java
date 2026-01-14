package dto;
import java.util.List;

public class OrdenRequest {
    private List<Long> productos; // IDs de los productos
    private String destino;

    // Getters y Setters
    public List<Long> getProductos() { return productos; }
    public void setProductos(List<Long> productos) { this.productos = productos; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
}
