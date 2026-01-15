package com.example.gestion_de_ordenes;

import dto.OrdenRequest;
import dto.ProductDTO;
import model.Orden;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdenResource {

    @PersistenceContext
    private EntityManager em;

    // CONFIGURACIÓN DE URLS 
    private final String URL_MS_PYTHON = "http://ms-python:8082/calcular-envio";
    private final String URL_MS_SPRING = "http://product-service:8081/api/products";

    @POST
    @Transactional
    public Response crearOrden(OrdenRequest request) {

        System.out.println("--- Iniciando Orden Inteligente ---");

        Client client = ClientBuilder.newClient();
        double subtotal = 0.0;

        // PASO 0: Agrupar productos (Lógica de Atomicidad)
        // Convertimos [1, 1, 1] en -> {ID: 1, Cantidad: 3}
        Map<Long, Integer> conteoProductos = new HashMap<>();
        for (Long id : request.getProductos()) {
            conteoProductos.put(id, conteoProductos.getOrDefault(id, 0) + 1);
        }

        // PASO 1: Llamar a Spring Boot (Una vez por cada producto distinto)
        for (Map.Entry<Long, Integer> entry : conteoProductos.entrySet()) {
            Long prodId = entry.getKey();
            int cantidadTotal = entry.getValue(); // Aquí ya pedimos el total (ej: 10)

            try {
                // Pedimos descontar el TOTAL de una sola vez
                String urlProducto = URL_MS_SPRING + "/" + prodId + "/reduce-stock?quantity=" + cantidadTotal;

                System.out.println("Solicitando al stock: ID " + prodId + ", Cantidad: " + cantidadTotal);

                Response respSpring = client.target(urlProducto)
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.json(""));

                if (respSpring.getStatus() != 200) {
                    System.out.println("❌ Stock insuficiente. Spring devolvió: " + respSpring.getStatus());
                    // Como falló el bloque entero, no se descontó nada. ¡Stock salvado!
                    return Response.status(409)
                            .entity("Stock insuficiente para el producto ID: " + prodId)
                            .build();
                }

                ProductDTO prod = respSpring.readEntity(ProductDTO.class);
                // Sumamos al subtotal: Precio Unitario * Cantidad
                subtotal += prod.getPrecio() * cantidadTotal;

            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(500).entity("Error interno: " + e.getMessage()).build();
            }
        }

        // PASO 2: Llamar a Python (Calculadora)
        double costoEnvio = 0.0;
        try {
            // Enviamos el destino real que viene del frontend
            String destino = request.getDestino() != null ? request.getDestino() : "Quito";

            JsonObject jsonPython = Json.createObjectBuilder()
                    .add("productos", Json.createArrayBuilder(request.getProductos()))
                    .add("destino", destino)
                    .build();

            Response respPython = client.target(URL_MS_PYTHON)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(jsonPython.toString()));

            if (respPython.getStatus() == 200) {
                JsonObject jsonResp = respPython.readEntity(JsonObject.class);
                costoEnvio = jsonResp.getJsonNumber("costo_envio").doubleValue();
            }
        } catch (Exception e) {
            System.out.println("Advertencia: Python falló, continuando sin costo de envío.");
        }

        // PASO 3: Guardar Orden
        Orden nuevaOrden = new Orden();
        nuevaOrden.setDestino(request.getDestino());
        nuevaOrden.setProductoIds(request.getProductos().toString());
        nuevaOrden.setTotal(subtotal + costoEnvio);
        em.persist(nuevaOrden);

        client.close();

        return Response.status(201)
                .entity("¡Orden exitosa! ID: " + nuevaOrden.getId() + ". Total pagado: $" + nuevaOrden.getTotal())
                .build();
    }
}