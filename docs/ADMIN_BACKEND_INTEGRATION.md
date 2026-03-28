# Módulo administrativo (Spring Boot + MongoDB)

## Resumen

Se añadió persistencia **MongoDB** para el catálogo gestionado por administración y se expusieron endpoints bajo **`/api/admin/*`**, protegidos con **`ROLE_ADMIN`** (JWT con claim `rol: ADMIN`, igual que ya mapea `TokenFilter` a `ROLE_ADMIN`).

El catálogo de productos que consume el **cliente público** (`/api/public/productos/...`) se lee desde la colección **`products`** en MongoDB. `ProductRepo` delega en **`ProductoDocumentRepository`** (sin Google Sheets).

## Requisitos

1. **MongoDB** en ejecución (local o Atlas).
2. Variable opcional **`MONGODB_URI`**; por defecto: `mongodb://localhost:27017/tienda_sana` (ver `application.properties`).

## Colecciones

| Colección           | Entidad Java                 |
|---------------------|------------------------------|
| `products`          | `ProductoDocument`           |
| `suppliers`         | `SupplierDocument`           |
| `product_lots`      | `ProductLotDocument`         |
| `restaurant_tables` | `RestaurantTableDocument`    |
| `reservation_tables` | `ClientMesaDocument` (mesas para reservas del cliente) |
| `accounts` | `CuentaDocument` |
| `shopping_carts` | `CarritoComprasDocument` |
| `product_sales` | `VentaProductoDocument` |
| `reservations` | `ReservaDocument` |
| `gestores_reservas` | `GestorReservaDocument` |

Índices declarados en anotaciones: `productId` y `supplierId` en `product_lots`, índices simples en `products` (nombre, active).

## Endpoints (todos requieren `Authorization: Bearer <JWT>` de usuario **ADMIN**)

### Proveedores — `/api/admin/suppliers`

- `GET` — Lista (activos e inactivos).
- `POST` — Crear (cuerpo según contrato con campo `"product"`).
- `PUT /{id}` — Actualizar.
- `DELETE /{id}` — Desactiva (`active=false`).

### Lotes e inventario — `/api/admin`

- `GET /lots?productId=` — Lista de lotes; filtro opcional por producto.
- `POST /lots` — Crea lote y **suma** `quantity` al `stockQuantity` del producto.
- `PUT /lots/{id}` — Actualiza lote y **ajusta** el stock del producto (delta o traslado entre productos).
- `GET /inventory` — Lista `{ productId, productName, stockQuantity }`.

### Productos (admin) — `/api/admin/products`

- `GET` — Todos (incluye inactivos).
- `POST` — Crear (stock inicial 0; se carga con lotes).
- `PUT /{id}` — Actualizar datos (no fuerza stock; opcional `outOfStock`).
- `DELETE /{id}` — Baja lógica (`active=false`).
- `PATCH /{id}/status` — Alterna `active` (visible / no visible en lógica de negocio).

### Mesas (admin, Mongo) — `/api/admin/tables`

- `GET`, `POST`, `PUT /{id}` — CRUD sobre **`restaurant_tables`**.
- `PATCH /{id}/status` — Cuerpo `{ "status": "AVAILABLE" | "RESERVED" | "OCCUPIED" }`.

**Nota:** Las mesas del flujo de **reservas del cliente** (`Mesa`, `MesaServiceImp`) están en **`reservation_tables`**. Las mesas **`restaurant_tables`** son el CRUD operativo del admin; son modelos distintos hasta que se decida unificar IDs o sincronización.

## Cliente público (productos)

`ProductoServiceImp` ahora usa **`ProductoDocumentRepository`**. Solo se listan/muestran productos con:

- `active == true`
- `outOfStock == false`
- `stockQuantity > 0`

## Pruebas con Postman

1. **Login** admin: `POST /api/auth/login` (cuenta con rol `ADMIN`).
2. Copiar el token JWT.
3. Peticiones a `/api/admin/...` con cabecera `Authorization: Bearer <token>`.

Si el usuario es `CLIENTE`, `TokenFilter` responde **403** en rutas `/api/admin/**`.

## Seguridad

- `SecurityConfig`: `requestMatchers("/api/admin/**").hasRole("ADMIN")`.
- Controladores admin: `@PreAuthorize("hasRole('ADMIN')")`.
- `TokenFilter` ya restringía `/api/admin` al rol ADMIN antes de llegar al `FilterChain`.

## Errores

- `GlobalExceptions` maneja `IllegalArgumentException` y `MethodArgumentNotValidException` con **400** y cuerpo `MessageDTO`.
