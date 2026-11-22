# FinTech Microservicios — Trabajo final

Ecosistema de microservicios en **Java 21 + Spring Boot 4.1 + Spring Cloud 2025.1** para la gestión de **clientes** y sus **productos financieros** (cuentas, tarjetas, préstamos e inversiones).

Diplomatura en Desarrollo de Software FinTech: IA y Microservicios — UTN-BA / Codeki.
Autora: Guillermina Cecha Scotti.

---

## Arquitectura

```
                         ┌──────────────────────┐
                         │   GitHub (este repo)  │
                         │   carpeta config-repo │
                         └──────────┬───────────┘
                                    │ lee los .yml
                         ┌──────────▼───────────┐
                         │    config-server      │  :8888
                         └──────────┬───────────┘
            configuración centralizada (puertos, URLs, credenciales)
         ┌──────────────────────────┼──────────────────────────┐
         ▼                          ▼                          ▼
┌─────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│  eureka-server  │◄──────│ customer-service │──────►│ product-service  │
│      :8761      │registro│      :8081       │ Feign │      :8082       │
└─────────────────┘◄──────│  MySQL customer_db│       │  MySQL product_db│
                  registro └──────────────────┘       └──────────────────┘
```

| Componente | Puerto | Responsabilidad |
|---|---|---|
| `config-server` | 8888 | Sirve la configuración de todos los servicios desde la carpeta `config-repo` de este repositorio en GitHub |
| `eureka-server` | 8761 | Service Registry: los servicios se registran y se descubren por nombre lógico |
| `customer-service` | 8081 | **Servicio principal.** Registra, consulta y actualiza clientes. Consulta y crea productos llamando a `product-service` con **Feign** |
| `product-service` | 8082 | **Servicio secundario.** Gestiona productos y expone los productos de cada cliente |
| `config-repo/` | — | Archivos `.yml` de configuración (no es un proyecto Java) |

### Decisiones de diseño

- **Una base de datos por microservicio** (`customer_db` y `product_db`): cada servicio es dueño de sus datos. `Producto` guarda el `clienteId` como referencia, sin clave foránea entre bases.
- **Comunicación declarativa con Feign** sin URLs fijas: `@FeignClient(name = "product-service")` le pide la ubicación a Eureka y Spring Cloud LoadBalancer elige una instancia.
- **Configuración centralizada:** cada servicio solo conoce su nombre y la URL del Config Server. Puertos, base de datos y Eureka viven en `config-repo/`.
- **Secretos fuera del repositorio:** la contraseña de MySQL se lee de la variable de entorno `DB_PASSWORD`.
- **Arquitectura en capas** en cada servicio: `controller → service → repository → model`, más `dto`, `mapper`, `exception`, `config` y `client`.
- **DTOs (records inmutables)** para entrada y salida: nunca se exponen las entidades JPA.
- **MapStruct** para pasar de DTO a entidad y viceversa, incluyendo un mapeo con **múltiples fuentes** (body + id de la URL).
- **Excepciones propias** y un `@RestControllerAdvice` por servicio con un formato de error único.
- **Swagger / OpenAPI** con `@ApiResponse` documentando los códigos de error.

---

## Requisitos

- JDK 21
- MySQL 8 o superior corriendo en `localhost:3306` (las bases se crean solas al arrancar)
- Git

## Cómo ejecutar

### 1. Variables de entorno (credenciales de MySQL)

En **cada** Terminal donde se levante `customer-service` o `product-service`:

```bash
export DB_PASSWORD=tu_contraseña_de_mysql
export DB_USERNAME=root      # opcional, por defecto es root
```

### 2. Levantar los servicios en este orden

Cada uno en una pestaña distinta de la Terminal (⌘ + T):

```bash
# 1) Config Server — siempre primero
cd config-server && ./mvnw spring-boot:run

# 2) Eureka Server
cd eureka-server && ./mvnw spring-boot:run

# 3) product-service
cd product-service && ./mvnw spring-boot:run

# 4) customer-service
cd customer-service && ./mvnw spring-boot:run
```

> **Sin acceso a GitHub:** el Config Server puede leer la carpeta local `config-repo` con el perfil `native`:
> `cd config-server && ./mvnw spring-boot:run -Dspring-boot.run.profiles=native`

### Atajo: scripts incluidos

| Script | Qué hace |
|---|---|
| `./compilar-todo.sh` | Compila y corre los tests de los 4 proyectos |
| `./levantar-todo.sh` | Levanta los 4 servicios en orden (logs en `logs/`). Con `native`, el Config Server lee la carpeta local |
| `./probar-endpoints.sh` | Prueba de punta a punta: alta de cliente, productos vía Feign y casos de error |
| `./detener-todo.sh` | Apaga los 4 servicios |

### 3. Verificar

| Qué | URL |
|---|---|
| Configuración que sirve el Config Server | http://localhost:8888/customer-service/default |
| Panel de Eureka (deben verse `CUSTOMER-SERVICE` y `PRODUCT-SERVICE` en estado UP) | http://localhost:8761 |
| Swagger de customer-service | http://localhost:8081/swagger-ui.html |
| Swagger de product-service | http://localhost:8082/swagger-ui.html |

### Tests

```bash
./compilar-todo.sh
```

Los tests usan **H2 en memoria** (`src/test/resources/application.yml`), con Config Server y Eureka deshabilitados: corren en cualquier máquina sin MySQL.

---

## Endpoints

### customer-service (`:8081`)

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/clientes` | Listar clientes | 200 |
| GET | `/clientes/{id}` | Buscar cliente | 200 / 404 |
| POST | `/clientes` | Registrar cliente | 201 / 400 / 409 |
| PUT | `/clientes/{id}` | Actualizar datos personales (el saldo no se edita) | 200 / 400 / 404 / 409 |
| POST | `/clientes/{id}/depositar` | Depositar en el saldo | 200 / 400 / 404 |
| POST | `/clientes/{id}/extraer` | Extraer del saldo | 200 / 400 / 404 / 422 |
| GET | `/clientes/{id}/productos` | **Cliente + sus productos** (Feign → product-service) | 200 / 404 / 503 |
| POST | `/clientes/{id}/productos` | **Asociar un producto** (Feign → product-service) | 201 / 400 / 404 / 409 / 503 |

### product-service (`:8082`)

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/productos` | Listar productos | 200 |
| GET | `/productos/{id}` | Buscar producto | 200 / 404 |
| GET | `/productos/cliente/{clienteId}` | **Productos de un cliente** (lo consume customer-service) | 200 |
| POST | `/productos` | Crear producto | 201 / 400 / 409 |
| PUT | `/productos/{id}` | Actualizar descripción, monto y estado | 200 / 400 / 404 |
| DELETE | `/productos/{id}` | Eliminar producto | 204 / 404 |

### Ejemplos

Registrar un cliente — `POST http://localhost:8081/clientes`
```json
{ "nombre": "Ana", "apellido": "García", "documento": "30111222", "email": "ana@mail.com", "saldo": 150000 }
```

Asociarle una tarjeta — `POST http://localhost:8081/clientes/1/productos`
```json
{ "tipo": "TARJETA", "numero": "4111-0001", "descripcion": "Visa Gold", "monto": 500000 }
```

Ver el cliente con sus productos — `GET http://localhost:8081/clientes/1/productos`
```json
{
  "cliente": { "id": 1, "nombre": "Ana", "apellido": "García", "documento": "30111222",
               "email": "ana@mail.com", "saldo": 150000.00, "fechaAlta": "2026-11-22T18:30:00" },
  "productos": [
    { "id": 1, "numero": "4111-0001", "tipo": "TARJETA", "descripcion": "Visa Gold",
      "monto": 500000.00, "estado": "ACTIVO", "clienteId": 1, "fechaAlta": "2026-11-22T18:31:00" }
  ]
}
```

Tipos de producto: `CUENTA`, `TARJETA`, `PRESTAMO`, `INVERSION`. Estados: `ACTIVO`, `INACTIVO`.

### Formato de error

```json
{ "fecha": "2026-11-22T18:35:00", "status": 404, "mensaje": "No se encontró el cliente con id 99" }
```

| Situación | Código |
|---|---|
| Datos inválidos | 400 |
| Cliente o producto inexistente | 404 |
| Documento o número de producto duplicado | 409 |
| Saldo insuficiente | 422 |
| product-service caído o no registrado en Eureka | 503 |

---

## Estructura del repositorio

```
fintech-microservicios/
├── config-repo/                 configuración centralizada
│   ├── application.yml          compartida (Eureka, JPA, Swagger)
│   ├── eureka-server.yml
│   ├── customer-service.yml
│   └── product-service.yml
├── config-server/
├── eureka-server/
├── customer-service/
│   └── src/main/java/com/fintech/customer/
│       ├── client/              ProductClient (Feign)
│       ├── config/  controller/  dto/  exception/
│       ├── mapper/              ClienteMapper, ProductoMapper (MapStruct)
│       ├── model/  repository/  service/
├── product-service/
│   └── src/main/java/com/fintech/product/
│       ├── config/  controller/  dto/  exception/
│       ├── mapper/  model/  repository/  service/
├── postman/                     colección para probar los endpoints
└── compilar-todo.sh             compila y testea los 4 proyectos
```

## Tecnologías

Java 21 · Spring Boot 4.1 · Spring Cloud 2025.1 (Config, Netflix Eureka, OpenFeign, LoadBalancer) · Spring Data JPA / Hibernate · MySQL · H2 (tests) · MapStruct 1.6 · springdoc-openapi 3 (Swagger) · JUnit 5 · Maven
