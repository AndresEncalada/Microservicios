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

import java.util.List;
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdenResource {

    @PersistenceContext
    private EntityManager em;

    // URLS de los otros microservicios (¡OJO: Cambiar esto para Docker!)
    private final String URL_MS_PYTHON = "http://ms-python:8082/calcular-envio";
    private final String URL_MS_SPRING = "http://product-service:8081/productos";

    @POST
    @Transactional // Importante para que guarde en la BD
    public Response crearOrden(OrdenRequest request) {

        System.out.println("--- Iniciando Orquestación de Orden ---");

        Client client = ClientBuilder.newClient();
        double subtotal = 0.0;

        // ----------------------------------------------------------------
        // PASO 1: Procesar Productos (Llamada a Spring Boot - MS1)
        // ----------------------------------------------------------------
        // Iteramos los IDs que nos mandó el frontend para pedirle al MS1 que descuente stock
        // y nos dé el precio real.

        for (Long prodId : request.getProductos()) {
            try {
                // Asumimos que tu compañero tiene un endpoint POST o PUT para descontar stock
                // Ej: POST http://ms-spring:8081/productos/1/reducir-stock
                String urlProducto = URL_MS_SPRING + "/" + prodId + "/reduce-stock?quantity=1";

                // Hacemos la llamada
                Response respSpring = client.target(urlProducto)
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.json("")); // Body vacío o cantidad si fuera necesario

                if (respSpring.getStatus() != 200) {
                    return Response.status(409)
                            .entity("Error: Stock insuficiente o producto no encontrado para ID: " + prodId)
                            .build();
                }

                // Leemos el producto actualizado para saber su precio
                ProductDTO prod = respSpring.readEntity(ProductDTO.class);
                subtotal += prod.getPrecio();

            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(500).entity("Error conectando con Microservicio de Productos").build();
            }
        }

        // ----------------------------------------------------------------
        // PASO 2: Calcular Envío (Llamada a Python - MS2)
        // ----------------------------------------------------------------
        double costoEnvio = 0.0;
        try {
            // Construimos el JSON para Python: {"productos": [1, 2], "destino": "Quito"}
            JsonObject jsonPython = Json.createObjectBuilder()
                    .add("productos", Json.createArrayBuilder(request.getProductos()))
                    .add("destino", request.getDestino())
                    .build();

            Response respPython = client.target(URL_MS_PYTHON)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(jsonPython.toString()));

            if (respPython.getStatus() == 200) {
                // Python responde: {"costo_envio": 5.50}
                JsonObject jsonResp = respPython.readEntity(JsonObject.class);
                // getJsonNumber devuelve un tipo complejo, por eso usamos doubleValue()
                costoEnvio = jsonResp.getJsonNumber("costo_envio").doubleValue();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Si falla Python, ¿cancelamos o asumimos costo 0?
            // Para el lab, mejor fallar para notar el error.
            return Response.status(500).entity("Error conectando con Calculadora de Envíos").build();
        }

        // ----------------------------------------------------------------
        // PASO 3: Guardar y Responder (Local - MS3)
        // ----------------------------------------------------------------

        Orden nuevaOrden = new Orden();
        nuevaOrden.setDestino(request.getDestino());
        // Guardamos los IDs como string simple para referencia
        nuevaOrden.setProductoIds(request.getProductos().toString());
        nuevaOrden.setTotal(subtotal + costoEnvio);

        // Guardar en MySQL
        em.persist(nuevaOrden);

        client.close(); // Cerramos conexiones HTTP

        return Response.status(201)
                .entity("Orden creada con ID: " + nuevaOrden.getId() + ". Total: $" + nuevaOrden.getTotal())
                .build();
    }
}
