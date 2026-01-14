package com.ups.product_service.entity;

import jakarta.persistence.*;
import lombok.Data; // Lombok genera getters, setters y toString automáticamente

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Double precio;
    private Integer stock;
}