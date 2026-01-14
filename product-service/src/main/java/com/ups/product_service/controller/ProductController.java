package com.ups.product_service.controller;

import com.ups.product_service.entity.Product;
import com.ups.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products") // Prefijo para todos los endpoints
public class ProductController {

    @Autowired
    private ProductRepository repository;

    // GET /api/products -> Listar todo
    @GetMapping
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    // GET /api/products/{id} -> Detalle
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/products -> Crear (Para llenar datos iniciales)
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return repository.save(product);
    }

    // POST /api/products/{id}/reduce-stock -> Para uso interno de microservicios
    @PostMapping("/{id}/reduce-stock")
    public ResponseEntity<String> reduceStock(@PathVariable Long id, @RequestParam int quantity) {
        Optional<Product> optionalProduct = repository.findById(id);

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            if (product.getStock() >= quantity) {
                product.setStock(product.getStock() - quantity);
                repository.save(product);
                return ResponseEntity.ok("Stock actualizado. Nuevo stock: " + product.getStock());
            } else {
                return ResponseEntity.badRequest().body("Stock insuficiente");
            }
        }
        return ResponseEntity.notFound().build();
    }
}