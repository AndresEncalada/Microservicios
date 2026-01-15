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

    // GET /api/orders -> lista todas las órdenes
    @GET
    public List<Orden> listarOrdenes() {
        return em.createQuery("SELECT o FROM Orden o ORDER BY o.fechaCreacion DESC", Orden.class)
                .getResultList();
    }

    // GET /api/orders/{id} -> obtiene una orden específica
    @GET
    @Path("/{id}")
    public Response obtenerOrden(@PathParam("id") Long id) {
        Orden orden = em.find(Orden.class, id);
        if (orden == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Orden no encontrada")
                    .build();
        }
        return Response.ok(orden).build();
    }

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
        double costoEnvio = 0.0;
        StringBuilder nombresProductos = new StringBuilder();

        try {
            // ===============================
            // PASO A: Llamar al servicio Python (costo de envío)
            // ===============================
            String destino = request.getDestino() != null ? request.getDestino() : "Quito";

            JsonObject jsonPython = Json.createObjectBuilder()
                    .add("productos", Json.createArrayBuilder(request.getProductos()))
                    .add("destino", destino)
                    .build();

            Response respPython = client.target(URL_MS_PYTHON)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(jsonPython.toString()));

            if (respPython.getStatus() != 200) {
                System.out.println("❌ Error desde MS-Python: " + respPython.getStatus());
                return Response.status(Response.Status.BAD_GATEWAY)
                        .entity("Fallo al calcular costo de envío")
                        .build();
            }

            JsonObject jsonResp = respPython.readEntity(JsonObject.class);
            costoEnvio = jsonResp.getJsonNumber("costo_envio").doubleValue();

            // ===============================
            // PASO B: Llamar a Spring Boot para descontar stock
            // ===============================
            // Agrupamos productos repetidos: [1,1,1] -> {1:3}
            Map<Long, Integer> conteoProductos = new HashMap<>();
            for (Long id : request.getProductos()) {
                conteoProductos.put(id, conteoProductos.getOrDefault(id, 0) + 1);
            }

            for (Map.Entry<Long, Integer> entry : conteoProductos.entrySet()) {
                Long prodId = entry.getKey();
                int cantidadTotal = entry.getValue();

                String urlProducto = URL_MS_SPRING + "/" + prodId + "/reduce-stock?quantity=" + cantidadTotal;
                System.out.println("Solicitando al stock: ID " + prodId + ", Cantidad: " + cantidadTotal);

                Response respSpring = client.target(urlProducto)
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.json(""));

                if (respSpring.getStatus() != 200) {
                    System.out.println("❌ Error descontando stock. Spring devolvió: " + respSpring.getStatus());
                    return Response.status(respSpring.getStatus())
                            .entity("No se pudo descontar stock para el producto ID: " + prodId)
                            .build();
                }

                ProductDTO prod = respSpring.readEntity(ProductDTO.class);
                subtotal += prod.getPrecio() * cantidadTotal;

                // Acumular nombres de productos para mostrarlos en el frontend
                if (nombresProductos.length() > 0) {
                    nombresProductos.append(", ");
                }
                nombresProductos.append(prod.getNombre());
            }

            // ===============================
            // PASO C: Guardar orden solo si A y B fueron exitosos
            // ===============================
            Orden nuevaOrden = new Orden();
            nuevaOrden.setDestino(request.getDestino());
            nuevaOrden.setProductoIds(request.getProductos().toString());
            nuevaOrden.setNombre(nombresProductos.toString());
            nuevaOrden.setTotal(subtotal + costoEnvio);
            em.persist(nuevaOrden);

            return Response.status(Response.Status.CREATED)
                    .entity("¡Orden exitosa! ID: " + nuevaOrden.getId() + ". Total pagado: $" + nuevaOrden.getTotal())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno: " + e.getMessage())
                    .build();

        } finally {
            client.close();
        }
    }
}
