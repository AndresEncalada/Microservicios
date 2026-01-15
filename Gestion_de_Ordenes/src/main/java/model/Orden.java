package model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes")
public class Orden implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Aquí guardamos los IDs de los productos como un String simple "1,2,5"
    // o podrías crear una tabla detalle, pero para este lab, simplifiquemos:
    private String productoIds;

    // Nombres de producto(s) asociados a la orden (ej: "Camiseta", "Camiseta, Pantalón")
    private String nombre;

    private String destino;
    private Double total;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductoIds() { return productoIds; }
    public void setProductoIds(String productoIds) { this.productoIds = productoIds; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
