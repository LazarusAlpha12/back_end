# 🚚 Backend de Seguimiento de Pedidos con Microservicios

Este repositorio contiene la implementación del backend para un sistema de seguimiento de pedidos y rutas, construido sobre una arquitectura de **microservicios** con **Spring Boot** y **API Gateway**. El frontend (React + Vite) se comunica exclusivamente con el API Gateway, que redirige las peticiones a los servicios correspondientes. La autenticación se basa en **JWT (JSON Web Tokens)** personalizados con firma HMAC-SHA256.

## 📌 Tabla de Contenidos

- [Arquitectura](#-arquitectura)
- [Tecnologías utilizadas](#-tecnologías-utilizadas)
- [Requisitos previos](#-requisitos-previos)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Instrucciones de ejecución local (con Docker)](#-instrucciones-de-ejecución-local-con-docker)
- [Pruebas con Postman](#-pruebas-con-postman)
- [Despliegue en Render](#-despliegue-en-render)
- [Diagrama de arquitectura](#-diagrama-de-arquitectura)
- [Contribuciones y flujo de trabajo en GitHub](#-contribuciones-y-flujo-de-trabajo-en-github)
- [Evidencias de pruebas](#-evidencias-de-pruebas)
- [Buenas prácticas aplicadas](#-buenas-prácticas-aplicadas)

---

## 🏗️ Arquitectura

El sistema sigue el estilo **cliente-servidor** y se compone de los siguientes microservicios, cada uno ejecutándose en su propio contenedor Docker. La seguridad se basa en **JWT custom** firmado con HMAC-SHA256, utilizando una clave secreta compartida.

| Servicio | Puerto interno | Rol Seguridad | Descripción |
|----------|----------------|---------------|-------------|
| **auth-service** | 8081 | Emisor JWT | Emite tokens JWT tras autenticar al usuario (email/password). Expone endpoints `/auth/login` y `/auth/register`. |
| **user-service** | 8082 | Resource Server | Gestiona usuarios y roles. Valida cada petición verificando la firma del JWT localmente con la clave secreta compartida. |
| **order-service** | 8083 | Resource Server | Gestiona pedidos, historial y ubicaciones. También valida JWT localmente como Resource Server. |
| **api-gateway** | 8080 | API Gateway | Actúa como punto único de entrada. Redirige peticiones y propaga el token JWT sin validarlo (la validación ocurre en los servicios). |
| **mysql-db** | 3306 | — | Base de datos compartida (MySQL 8). Las tablas se crean automáticamente mediante Hibernate. |

**Flujo de autenticación**:

1. El frontend envía credenciales (email y password) al endpoint `/auth/login` del `auth-service`.
2. El `auth-service` valida las credenciales contra la base de datos y devuelve un JWT firmado con su clave secreta HMAC-SHA256. El token incluye un identificador único (`jti`) para permitir su revocación individual.
3. El frontend incluye el token en cada petición al API Gateway (header `Authorization: Bearer <token>`).
4. El gateway reenvía la petición al microservicio correspondiente.
5. `user-service` y `order-service` validan el token automáticamente verificando la firma con la misma clave secreta (sin consultar al `auth-service`).
6. Al cerrar sesión, el frontend envía `POST /auth/logout` con el token. El `auth-service` registra el `jti` en la tabla `token_blacklist` — el token queda revocado de inmediato aunque aún no haya expirado.

**Duración de los tokens**: 8 horas desde el login. Pueden invalidarse antes haciendo logout.

---
### Base de datos compartida (simplificación académica)

Aunque en una arquitectura de microservicios pura cada servicio debería tener su propia base de datos, por razones de simplicidad y tiempo usamos una única instancia MySQL. **Cada servicio accede solo a sus tablas**:
- `auth-service` → tablas `personas`, `repartidores`, `operadores_logisticos`, `token_blacklist`.
- `user-service` → tabla `personas` (gestión completa de usuarios).
- `order-service` → tablas `pedidos`, `historial_movimiento`, `ubicaciones`.

No se utilizan **triggers** ni sincronización a nivel de base de datos. Si un servicio necesita datos de otro (ej. `order-service` necesita el nombre del cliente), se hará mediante llamada a la API de `user-service` o se mantendrá una copia desnormalizada gestionada por eventos.

### Esquema de base de datos

El modelo usa una tabla principal `personas` con **tablas de extensión** para los roles que tienen datos propios adicionales:

```
personas (tabla principal — todos los usuarios)
├── id           BIGINT PK AUTO_INCREMENT
├── nombre       VARCHAR(255) NOT NULL
├── apellido     VARCHAR(255)
├── email        VARCHAR(255) UNIQUE NOT NULL
├── password     VARCHAR(255) NOT NULL  ← BCrypt hash
└── rol          VARCHAR(255) NOT NULL  ← ADMINISTRADOR | OPERADOR_LOGISTICO | REPARTIDOR | CLIENTE

repartidores (extensión — solo repartidores)
├── persona_id      BIGINT PK FK → personas.id
├── capacidad       INT NOT NULL
├── disponibilidad  BOOLEAN NOT NULL DEFAULT true
├── telefono        VARCHAR(20)
├── vehiculo        VARCHAR(255)
└── estado          VARCHAR(50) DEFAULT 'DISPONIBLE'  ← DISPONIBLE | OCUPADO | INACTIVO

operadores_logisticos (extensión — solo operadores)
├── persona_id   BIGINT PK FK → personas.id
└── admin_id     BIGINT FK → personas.id  ← admin que supervisa al operador

token_blacklist (tokens revocados por logout)
├── jti          VARCHAR(36) PK  ← UUID del JWT
└── expires_at   DATETIME NOT NULL

pedidos
├── id            BIGINT PK AUTO_INCREMENT
├── version       INT  ← optimistic locking
├── origen        VARCHAR(255) NOT NULL
├── destino       VARCHAR(255) NOT NULL
├── descripcion   VARCHAR(255) NOT NULL
├── estado        VARCHAR(255) NOT NULL  ← PENDIENTE | ASIGNADO | EN_TRANSITO | ENTREGADO | CANCELADO
├── cliente_id    BIGINT NOT NULL
├── repartidor_id BIGINT
└── fecha_creacion DATETIME NOT NULL

historial_movimiento
├── id           BIGINT PK AUTO_INCREMENT
├── pedido_id    BIGINT NOT NULL FK → pedidos.id
├── ubicacion_id BIGINT FK → ubicaciones.id
├── operador_id  BIGINT
├── tipo_evento  VARCHAR(50) NOT NULL  ← CREADO | ESTADO_CAMBIADO | ASIGNADO | UBICACION_ACTUALIZADA
├── estado       VARCHAR(255) NOT NULL
├── observacion  VARCHAR(255)
└── fecha_hora   DATETIME NOT NULL

ubicaciones
├── id            BIGINT PK AUTO_INCREMENT
├── direccion     VARCHAR(255) NOT NULL
├── ubicacion_lat DOUBLE
└── ubicacion_lng DOUBLE

configuracion (parámetros del sistema)
├── id           BIGINT PK AUTO_INCREMENT
├── clave        VARCHAR(255) UNIQUE NOT NULL
├── valor        VARCHAR(255) NOT NULL
├── descripcion  VARCHAR(255)
└── tipo         VARCHAR(50)  ← boolean | number | string

audit_logs (logs del sistema)
├── id           BIGINT PK AUTO_INCREMENT
├── timestamp    DATETIME NOT NULL
├── tipo         VARCHAR(50) NOT NULL  ← INFO | WARN | ERROR
├── usuario_id   BIGINT
└── descripcion  VARCHAR(255)
```

Hibernate genera y actualiza estas tablas automáticamente (`ddl-auto: update`) al arrancar cada servicio.

---

## 📡 Nuevos Endpoints (Admin Module)

### user-service
- **GET** `/api/usuarios/repartidores` — Lista repartidores con datos completos. Requiere ADMINISTRADOR u OPERADOR_LOGISTICO.
- **GET** `/api/config` — Parametros de configuracion del sistema. Solo ADMINISTRADOR.
- **PUT** `/api/config/{id}` — Actualiza un parametro. Solo ADMINISTRADOR.
- **GET** `/api/logs?limit=100` — Logs del sistema. Solo ADMINISTRADOR.

### order-service
- **GET** `/api/pedidos/reportes` — Estadisticas de pedidos por estado. Solo ADMINISTRADOR.
- **GET** `/api/pedidos/{pedidoId}/historial` — Historial de eventos. ADMINISTRADOR, OPERADOR_LOGISTICO o REPARTIDOR.

---

## 🧰 Tecnologías utilizadas

- **Java ver >= 17**
- **Spring Boot 4.0.x**
- **Spring Security & JJWT 0.12.x** (para autenticación JWT)
- **Spring Cloud Gateway** (API Gateway reactivo)
- **Spring Data JPA (Hibernate)**
- **MySQL 8**
- **Maven**
- **Docker & Docker Compose**
- **Postman** (pruebas de API)
- **Git / GitHub**

---

## 📋 Requisitos previos

- **Java 17 or higher** (Open JDK)
- **Maven** (o usar `./mvnw`)
- **Docker Desktop** (con integración WSL2 en Windows) o Docker Engine + Compose
- **Git**

---

## 📁 Estructura del proyecto

```
back_end/
├── auth-service/                 # Emisor de JWT (autenticación)
│   ├── src/main/java/auth/
│   │   ├── controller/           # Endpoints públicos: POST /auth/login, POST /auth/register, POST /auth/logout
│   │   ├── service/              # Lógica de negocio: AuthService, JwtService, CustomUserDetailsService
│   │   ├── repository/           # PersonaRepository, RepartidorRepository, OperadorLogisticoRepository, TokenBlacklistRepository
│   │   ├── entity/               # Persona, Repartidor, OperadorLogistico, enum Rol, TokenBlacklist
│   │   ├── dto/                  # DTOs: LoginRequest, LoginResponse, RegisterRequest
│   │   ├── config/               # SecurityConfig, JwtAuthenticationFilter (verifica blacklist)
│   │   ├── scheduled/            # TokenCleanupTask: limpieza automática de tokens expirados (cada hora)
│   │   └── exception/            # GlobalExceptionHandler (401, 409, 500)
│   ├── Dockerfile                # Instrucciones para construir la imagen Docker del auth-service
│   └── pom.xml                   # Dependencias: Spring Security, JJWT, Data JPA, MySQL
│
├── user-service/                 # Resource Server (gestión de usuarios, repartidores, configuración, logs)
│   ├── src/main/java/user/
│   │   ├── controller/           # Endpoints protegidos:
│   │   │   ├── PersonaController       # CRUD de usuarios + GET /api/usuarios/repartidores
│   │   │   ├── ConfiguracionController # GET/PUT /api/config (parámetros del sistema)
│   │   │   └── AuditLogController      # GET /api/logs (logs del sistema)
│   │   ├── service/              # Lógica de negocio de usuarios + auditoría
│   │   ├── repository/           # PersonaRepository, RepartidorRepository, ConfiguracionRepository, AuditLogRepository
│   │   ├── entity/               # Persona, Repartidor, Configuracion, AuditLog
│   │   ├── dto/                  # PersonaResponseDTO, RepartidorResponseDTO, etc.
│   │   ├── config/               # SecurityConfig (valida JWT usando la misma clave secreta)
│   │   └── exception/            # Manejo de errores
│   ├── Dockerfile
│   └── pom.xml                   # Dependencias: Spring Security, JJWT, Data JPA, MySQL
│
├── order-service/                # Resource Server (pedidos, historial, ubicaciones, reportes)
│   ├── src/main/java/order/
│   │   ├── controller/           # Endpoints protegidos:
│   │   │   ├── PedidoController       # CRUD pedidos, cambiar estado, asignar repartidor, registrar ubicación
│   │   │   │                          # GET /api/pedidos/reportes (estadísticas de pedidos)
│   │   │   └── HistorialController    # GET /api/pedidos/{id}/historial (eventos de un pedido)
│   │   ├── service/              # PedidoService, HistorialService (lógica de negocio, optimistic locking, auditoría)
│   │   ├── repository/           # PedidoRepository, HistorialRepository, UbicacionRepository (JPA)
│   │   ├── entity/               # Pedido (con @Version), Historial, Ubicacion, EstadoPedido (enum)
│   │   ├── dto/                  # PedidoRequestDTO, PedidoResponseDTO, HistorialFiltroDTO, UbicacionRequestDTO
│   │   ├── config/               # SecurityConfig: Resource Server JWT (misma configuración que user-service)
│   │   └── exception/            # GlobalExceptionHandler (OptimisticLockException → 409, etc.)
│   ├── Dockerfile
│   └── pom.xml                   # Mismas dependencias que user-service
│
├── api-gateway/                  # Punto único de entrada (Spring Cloud Gateway)
│   ├── src/main/java/gateway/
│   │   ├── config/               # GatewayConfig: define rutas (/auth/** → auth-service, /api/usuarios/** → user-service, /api/pedidos/** → order-service), timeouts, CORS, filtros (logs, etc.)
│   │   └── filter/               # (Opcional) Filtros personalizados, por ejemplo para registrar cada petición o añuir headers
│   ├── Dockerfile
│   └── pom.xml                   # Dependencias: Spring Cloud Gateway (no incluye Spring Web, son incompatibles)
│
├── docker-compose.yml            # Orquestación de todos los contenedores: mysql-db, auth-service, user-service, order-service, api-gateway. Define red interna, volúmenes, variables de entorno.
├── .gitignore                    # Archivos y carpetas ignoradas por Git: target/, .idea/, .DS_Store, application-secrets.yml, etc.
└── README.md                     # Documentación del proyecto: arquitectura, instrucciones de ejecución, pruebas con Postman, diagrama, etc.
```

Cada microservicio sigue el patrón Controller → Service → Repository → Entity, utilizando DTOs para la comunicación con el exterior.

---

## 🚀 Instrucciones de ejecución local (con Docker)

1. **Clonar el repositorio**
   ```bash
   git clone git@github.com:tu-usuario/back_end.git
   cd back_end
   ```

2. **Construir los JAR de cada servicio** (opcional, los Dockerfiles pueden hacer multi‑stage)
   ```bash
   cd auth-service && ./mvnw clean package && cd ..
   cd user-service && ./mvnw clean package && cd ..
   cd order-service && ./mvnw clean package && cd ..
   cd api-gateway && ./mvnw clean package && cd ..
   ```

3. **Levantar todos los contenedores**
   ```bash
   docker compose up --build
   ```
   Este comando levanta los cinco contenedores, crea una red interna y expone los puertos:
   - Gateway: `8080`
   - auth-service: `8081`
   - user-service: `8082`
   - order-service: `8083`
   - MySQL: `3306`

4. **Verificar el estado**
   ```bash
   docker compose ps
   ```

5. **Obtener un token JWT** (para probar desde Postman)
   - El `auth-service` expone el endpoint:
     ```
     POST http://localhost:8081/auth/login
     ```
   - Body (JSON):
     ```json
     { "email": "admin@test.com", "password": "password123" }
     ```

   Respuesta:
   ```json
   {
     "token": "eyJhbGc...",
     "id": 1,
     "nombre": "Admin",
     "email": "admin@test.com",
     "rol": "ADMINISTRADOR"
   }
   ```
   > Nota: El rol en JWT se devuelve en MAYUSCULAS: ADMINISTRADOR, OPERADOR_LOGISTICO, REPARTIDOR, CLIENTE

6. **Acceder a los recursos protegidos**
   - Incluye el token en el header: `Authorization: Bearer <access_token>`
   - Ejemplo: `GET http://localhost:8080/api/usuarios`

Para detener los contenedores:
```bash
docker compose down
```
Ejemplo de docker-compose.yml con healthchecks
```
yaml
version: '3.8'
services:
  mysql-db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: tracking_db
      MYSQL_ROOT_PASSWORD: rootpass
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - pedidos-net

  auth-service:
    build: ./auth-service
    ports:
      - "8081:8080"
    environment:
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      mysql-db:
        condition: service_healthy
    networks:
      - pedidos-net

  # user-service y order-service similares, con depends_on a mysql-db (service_healthy)
  # api-gateway depende de auth-service, user-service, order-service
```
---

## 🧪 Pruebas con Postman

Se incluye una colección actualizada en la raíz: `PedidosTracking_OAuth2.postman_collection.json`. El flujo de pruebas es:

1. **Crear usuario** o **Iniciar sesión** (email y password) para obtener el JWT.
2. **Usar token** para invocar endpoints protegidos a través del gateway o directo.

### Solicitud de token (Login)

- **URL**: `http://localhost:8081/auth/login`
- **Método**: POST
- **Headers**: `Content-Type: application/json`
- **Body** (Raw JSON): 
  ```json
  {
    "email": "admin@test.com",
    "password": "password123"
  }
  ```
- **Respuesta**: contiene el `token` y datos del usuario. Copiar el token.

### Registro de usuarios (por rol)

- **URL**: `http://localhost:8081/auth/register`
- **Método**: POST

**Administrador o Cliente** (sin campos extra):
```json
{
  "nombre": "Admin", "apellido": "Principal",
  "email": "admin@test.com", "password": "pass123",
  "rol": "ADMINISTRADOR"
}
```

**Repartidor** (`capacidad` obligatorio, otros opcionales):
```json
{
  "nombre": "Carlos", "apellido": "López",
  "email": "carlos@test.com", "password": "pass123",
  "rol": "REPARTIDOR",
  "capacidad": 10,
  "disponibilidad": true,
  "telefono": "3001234567",
  "vehiculo": "Moto Honda",
  "estado": "DISPONIBLE"
}
```

**Operador Logístico** (`adminId` obligatorio — debe ser ID de un administrador existente):
```json
{
  "nombre": "Ana", "apellido": "Gómez",
  "email": "ana@test.com", "password": "pass123",
  "rol": "OPERADOR_LOGISTICO",
  "adminId": 1
}
```

Errores comunes: `400` si falta `capacidad` en un repartidor, o si `adminId` no existe o no es administrador.

### Llamada a un endpoint protegido (Resource Server)

- **URL**: `http://localhost:8080/api/pedidos` (pasa por el gateway)
- **Método**: GET
- **Headers**: `Authorization: Bearer <token>`

Esperar respuesta `200 OK` con lista de pedidos (vacía al principio).

### Cerrar sesión (Logout)

- **URL**: `http://localhost:8081/auth/logout`
- **Método**: POST
- **Headers**: `Authorization: Bearer <token>`
- **Respuesta**: `200 OK` → `{ "message": "Sesión cerrada correctamente" }`

El token queda revocado inmediatamente. Cualquier petición posterior con ese token devuelve `401 Token revocado`, aunque el token todavía no haya expirado.

### Validación de errores

- Token inválido o ausente → `401 Unauthorized`
- Token expirado → `401 Unauthorized`
- Token revocado (logout previo) → `401 Token revocado`
- Rol insuficiente (si se implementa) → `403 Forbidden`

Los tiempos de respuesta se mantienen por debajo de 2 segundos gracias a la validación local de JWT en los Resource Servers (sin llamadas al `auth-service` en cada petición).

### Nuevos endpoints para Admin Module

**Repartidores** — Listar todos los repartidores
```
GET http://localhost:8080/api/usuarios/repartidores
Authorization: Bearer <token>
```
Respuesta: Lista de repartidores con teléfono, vehículo y estado.

**Configuracion** — Obtener/actualizar parámetros del sistema
```
GET http://localhost:8080/api/config
Authorization: Bearer <token>
```

```
PUT http://localhost:8080/api/config/{id}
Authorization: Bearer <token>
Content-Type: application/json

{ "valor": "nuevo_valor" }
```

**Logs** — Ver logs del sistema
```
GET http://localhost:8080/api/logs?limit=100
Authorization: Bearer <token>
```

**Reportes** — Estadísticas de pedidos
```
GET http://localhost:8080/api/pedidos/reportes
Authorization: Bearer <token>
```
Respuesta: `{ "totalPedidos": 5, "porEstado": { "PENDIENTE": 2, "EN_TRANSITO": 1, ... } }`

**Historial de Pedido** — Ver eventos de un pedido
```
GET http://localhost:8080/api/pedidos/42/historial
Authorization: Bearer <token>
```

---

## ☁️ Despliegue en Render

Render permite desplegar un `docker-compose.yml` como Blueprint. Pasos:

1. Subir el repositorio a GitHub.
2. En Render, crear un nuevo **Blueprint** y conectar el repo.
3. Asegurarse de configurar las siguientes variables de entorno:
   - `APP_JWT_SECRET` — clave HMAC-SHA256 (mínimo 256 bits). Debe ser la misma en todos los servicios que validen tokens.
4. Render construirá y levantará los contenedores automáticamente.
5. Actualizar el frontend para que apunte a la URL pública de Render (puerto 8080).

> **Nota**: Para entornos de producción, se recomienda usar una base de datos externa (ej. Clever Cloud) en lugar del volumen efímero de Docker.

---

## 📐 Diagrama de arquitectura

```mermaid
graph TD
    Client[Frontend React] -->|POST /auth/login| Auth[auth-service :8081]
    Client -->|API calls con Bearer token| Gateway[API Gateway :8080]
    Gateway -->|/api/usuarios/**| User[user-service :8082]
    Gateway -->|/api/pedidos/**| Order[order-service :8083]
    Auth --> DB[(MySQL :3306)]
    User --> DB
    Order --> DB
    User -.->|Local Secret Validation| JWT_Secret((Shared Secret))
    Order -.->|Local Secret Validation| JWT_Secret
```

Los Resource Servers validan el JWT de forma local y stateless utilizando la misma clave secreta (HMAC-SHA256) configurada en sus archivos `application.yaml`, sin necesidad de comunicarse con el `auth-service`.

---

## 👥 Contribuciones y flujo de trabajo en GitHub

- La rama `main` está protegida mediante **Rulesets**.
- No se permite push directo a `main`; todo cambio debe realizarse mediante **Pull Requests** con al menos 1 aprobación.
- Las conversaciones deben resolverse antes de fusionar.

Flujo recomendado:
```bash
git checkout main
git pull origin main
git checkout -b feature/nombre
... (commits)
git push --set-upstream origin feature/nombre
```
Luego abrir Pull Request en GitHub.

---

## 📸 Evidencias de pruebas

Las capturas de pantalla de las pruebas con Postman se encuentran en la carpeta `/docs`:

- `postman-token-request.png` – Solicitud de token exitosa.
- `postman-listar-pedidos.png` – Listado de pedidos con token válido.
- `postman-error-401.png` – Token inválido.
- `postman-error-409.png` – Conflicto por optimistic locking.

También se incluye el archivo de colección exportado `PedidosTracking_OAuth2.postman_collection.json`.

---

## 🧠 Buenas prácticas aplicadas

- **Autenticación Stateless** – Uso de JWT para evitar manejo de sesiones en servidor.
- **Validación descentralizada** – Los Resource Servers validan los tokens localmente sin acoplarse al `auth-service`.
- **Revocación de tokens (Blacklist)** – El `jti` (JWT ID) único en cada token permite invalidarlo al hacer logout. La tabla `token_blacklist` en MySQL actúa como blacklist compartida entre todos los servicios. Los tokens expirados se eliminan automáticamente cada hora (`@Scheduled`).
- **Separación de responsabilidades** – Cada microservicio maneja su propio dominio de datos.
- **Optimistic locking** (`@Version`) en entidades `Pedido`.
- **Contenerización** con Docker Compose.
- **Manejo global de excepciones** (`@RestControllerAdvice`).
- **Uso de DTOs** para no exponer entidades JPA.

---

## 📄 Licencia

Proyecto académico – Universidad Militar Nueva Granada. Sin fines comerciales.

---

## ✒️ Autores

- Jorge Enrique Celis Cortés
- Liner Fabian Candia Marin
- Miguel Eduardo Parra Amador
- Santiago Andres Diaz Peña
