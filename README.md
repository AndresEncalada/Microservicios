# 📦 Sistema de Microservicios: E-commerce de Gestión de Órdenes

Este proyecto es una aplicación distribuida basada en arquitectura de **Microservicios** para simular un flujo de e-commerce. Permite visualizar un catálogo de productos, gestionar inventario en tiempo real y procesar órdenes de compra, garantizando la persistencia de datos mediante contenedores Docker.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Payara](https://img.shields.io/badge/Payara_Micro-004481?style=for-the-badge&logo=jakarta-ee&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

---

## 🏗️ Componentes y su Función

El sistema está orquestado mediante **Docker Compose** y se divide en los siguientes servicios:

| Componente | Tecnología | Puerto | Función Principal |
| :--- | :--- | :--- | :--- |
| **Frontend** | React + Vite | `5173` | **Interfaz de Usuario:** Muestra el catálogo de productos y el carrito de compras. Se comunica con los microservicios para leer datos y enviar órdenes. |
| **Product Service** | Java (Spring Boot) | `8081` | **Gestión de Inventario:** API REST que administra el catálogo de productos. Se encarga de consultar y actualizar el stock disponible. |
| **Order Service** | Java (Payara Micro) | `8083` | **Gestión de Ventas:** Microservicio Jakarta EE que procesa la creación de órdenes, calcula totales y registra la transacción. |
| **Database (Postgres)** | PostgreSQL 13 | `5432` | **Persistencia de Productos:** Base de datos relacional dedicada al servicio de productos. |
| **Database (MySQL)** | MySQL 8.0 | `3306` | **Persistencia de Órdenes:** Base de datos relacional dedicada al historial de ventas. |

---

## 🚀 Funcionamiento de la Página Web

El flujo de la aplicación simula una compra real:

1.  **Carga del Catálogo:**
    * Al iniciar la web, el **Frontend** consume la API del `Product Service`.
    * Recupera la lista de productos, precios y **stock actual** desde PostgreSQL.

2.  **Selección de Producto:**
    * El usuario visualiza los productos y selecciona la cantidad deseada.
    * El sistema valida visualmente que exista stock disponible.

3.  **Procesamiento de Compra:**
    * Al hacer clic en "Comprar", el Frontend envía una solicitud POST al `Order Service`.
    * El `Order Service` calcula el monto total y **guarda la orden** permanentemente en MySQL.

4.  **Actualización de Inventario:**
    * Simultáneamente, se actualiza el stock en el `Product Service`, restando la cantidad comprada en la base de datos PostgreSQL.
    * Si el usuario refresca la página, verá el stock disminuido.

---

## 🛠️ Instrucciones de Despliegue

Sigue estos pasos para ejecutar el proyecto en tu entorno local. No necesitas tener Java o Node.js instalados, solo Docker.

### 📋 Prerrequisitos
* **Docker** y **Docker Compose** instalados.
* **Git** para clonar el repositorio.

### 1. Clonar el Repositorio
``` bash
git clone https://github.com/TU_USUARIO/Microservicios.git
cd Microservicios
```

### 2. Compilar los Microservicios (Build)
Para asegurar que se ejecuta la última versión del código y evitar errores de versiones, utilizaremos contenedores temporales de Maven para compilar los archivos `.jar` y `.war`.

**Compilar Product Service (Spring Boot):**
Ejecuta este comando en la raíz del proyecto:

``` bash
docker run --rm -v "%cd%/product-service:/app" -w /app maven:3.8.5-openjdk-17 mvn clean package
```

**Compilar Order Service (Payara/Jakarta EE):**
Ejecuta este comando en la raíz del proyecto:

``` bash
docker run --rm -v "%cd%/Gestion_de_Ordenes:/app" -w /app maven:3.8.5-openjdk-17 mvn clean package
```

*(Nota: Si usas Linux, Mac o PowerShell, cambia `%cd%` por `${PWD}`).*

### 3. Levantar la Infraestructura
Una vez generados los ejecutables nuevos, inicia todos los contenedores con Docker Compose:

``` bash
docker-compose up --build
```

*Espera unos minutos hasta que veas en los logs que los servicios han iniciado correctamente.*

### 4. Acceder a la Aplicación

Una vez que los servicios estén activos, tienes diferentes puntos de acceso según tu rol:

**👤 Para el Usuario Final (Interfaz Gráfica):**
* **Tienda Web:** [http://localhost:5173](http://localhost:5173)
    *(Desde aquí se realiza el flujo completo de compra).*

**👨‍💻 Para Desarrolladores (Pruebas de API):**
Puedes acceder directamente a los microservicios para verificar que responden con JSON (útil para debugging):
* **Catálogo (JSON):** [http://localhost:8081/api/products](http://localhost:8081/api/products)
* **Órdenes (JSON):** [http://localhost:8083/api/orders](http://localhost:8083/api/orders)

## 💾 Notas sobre Persistencia

Este proyecto utiliza **Docker Volumes** para asegurar que los datos no se pierdan al reiniciar los contenedores:
* `postgres_data`: Mantiene el inventario y precios.
* `mysql_data`: Mantiene el historial de órdenes creadas.

Además, la configuración de JPA/Hibernate está optimizada (`update` y `create-or-extend-tables`) para respetar los datos existentes entre reinicios.
