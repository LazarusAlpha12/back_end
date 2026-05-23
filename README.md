# 🚚 Backend de Seguimiento de Pedidos con Microservicios

Este repositorio contiene la implementación del backend para un sistema de seguimiento de pedidos y rutas, construido sobre una arquitectura de **microservicios** con **Spring Boot**, **API Gateway**, **JWT** para autenticación y **Docker Compose** para orquestación. El frontend (React + Vite) se comunica exclusivamente con el API Gateway, que redirige las peticiones a los servicios correspondientes.

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

## 🏗️ Arquitectura

El sistema sigue el estilo **cliente-servidor** y se compone de los siguientes microservicios, cada uno ejecutándose en su propio contenedor Docker. El frontend (React) se comunica únicamente con el **API Gateway**, quien actúa como punto único de entrada y oculta la topología interna.

| Servicio | Puerto interno | Descripción |
|----------|----------------|-------------|
| **auth-service** | 8081 | Registro de usuarios, inicio de sesión y emisión de tokens JWT. Almacena la información básica de autenticación (tabla `personas`). |
| **user-service** | 8082 | CRUD completo de usuarios y gestión de roles (administrador, operador logístico, repartidor, cliente). |
| **order-service** | 8083 | Gestión de pedidos, historial de movimientos, asignación de repartidores y actualización de ubicaciones. |
| **api-gateway** | 8080 | Punto único de entrada. Enruta `/auth/**` → auth-service, `/api/usuarios/**` → user-service, `/api/pedidos/**` → order-service. Además configura timeouts, CORS y una caché simple. |
| **mysql-db** | 3306 | Base de datos relacional compartida (MySQL 8). Las tablas son creadas automáticamente por Hibernate gracias a `ddl-auto=update`. |

La comunicación entre servicios es **síncrona vía HTTP** a través del API Gateway. La autenticación se realiza mediante JWT: el `auth-service` emite el token y los demás servicios (`user-service`, `order-service`) lo validan localmente usando la misma clave secreta, sin necesidad de llamadas adicionales. Esto mantiene la arquitectura stateless.

---

## 🧰 Tecnologías utilizadas

- **Java 17** – Lenguaje base.
- **Spring Boot 3.1.x** – Framework principal.
- **Spring MVC** – Exposición de endpoints REST.
- **Spring Data JPA (Hibernate)** – Mapeo objeto‑relacional y consultas.
- **Spring Security + JWT (JJWT)** – Autenticación y autorización.
- **Spring Cloud Gateway** – API Gateway reactivo (no bloqueante).
- **MySQL 8** – Base de datos relacional.
- **Maven** – Gestión de dependencias y construcción.
- **Docker & Docker Compose** – Contenerización y orquestación.
- **Postman** – Pruebas de API.
- **Git / GitHub** – Control de versiones y colaboración.

---

## 📋 Requisitos previos

Para ejecutar el proyecto en local es necesario tener instalado:

- **Java 17** (JDK)
- **Maven** (opcional, también se puede usar el wrapper `./mvnw`)
- **Docker Desktop** (con integración WSL2 en Windows) o Docker Engine + Docker Compose
- **Git** (para clonar el repositorio)

---

## 📁 Estructura del proyecto

El repositorio está organizado como un **monorepo** que contiene cuatro microservicios independientes, cada uno con su propia estructura Maven y su `Dockerfile`. La base de datos se declara como un servicio más en `docker-compose.yml`.

```
back_end/
├── auth-service/                 # Microservicio de autenticación
│   ├── src/main/java/auth/
│   │   ├── controller/           # Endpoints REST
│   │   ├── service/              # Lógica de negocio
│   │   ├── repository/           # JPA repositories
│   │   ├── entity/               # Entidades JPA
│   │   ├── dto/                  # Data Transfer Objects
│   │   ├── config/               # Seguridad, JWT, beans
│   │   └── exception/            # Manejador global de excepciones
│   ├── Dockerfile
│   └── pom.xml
├── user-service/                 # Misma estructura que auth-service
├── order-service/                # Misma estructura
├── api-gateway/                  # Configuración de rutas y filtros
├── docker-compose.yml
├── .gitignore
└── README.md
```

> **Nota**: Los directorios vacíos (por ejemplo, `controller/`, `service/`, etc.) se mantienen en el repositorio gracias a un archivo `.gitkeep`. Una vez que se agreguen las primeras clases Java, ese archivo puede eliminarse.

Cada microservicio sigue el patrón de capas:
- **Controller** – Maneja las peticiones HTTP, valida datos de entrada y llama al servicio. Nunca contiene lógica de negocio.
- **Service** – Contiene las reglas de negocio, transacciones y orquestación de repositorios.
- **Repository** – Acceso a base de datos (CRUD y consultas personalizadas).
- **Entity** – Mapeo JPA de las tablas.
- **DTO** – Objetos de transferencia de datos, utilizados en las peticiones y respuestas para no exponer las entidades.
- **Config** – Configuración de Spring Security, JWT y otros beans.
- **Exception** – Manejo global de excepciones con `@RestControllerAdvice`.

---

## 🚀 Instrucciones de ejecución local (con Docker)

Sigue estos pasos para levantar todo el sistema en tu máquina:

1. **Clonar el repositorio**
   ```bash
   git clone git@github.com:tu-usuario/back_end.git
   cd back_end
   ```

2. **Construir los JAR de cada servicio** (opcional, ya que el `Dockerfile` puede hacerlo con multi‑stage, pero se recomienda para verificar errores)
   ```bash
   cd auth-service && ./mvnw clean package && cd ..
   cd user-service && ./mvnw clean package && cd ..
   cd order-service && ./mvnw clean package && cd ..
   cd api-gateway && ./mvnw clean package && cd ..
   ```

3. **Levantar todos los contenedores con Docker Compose**
   ```bash
   docker compose up --build
   ```
   Este comando:
   - Descarga la imagen oficial de MySQL 8.
   - Construye las imágenes de cada microservicio (usando sus `Dockerfile`).
   - Crea una red interna (`pedidos-net`) para la comunicación entre contenedores.
   - Levanta los cinco contenedores y expone los puertos al host:
     - Gateway → `8080`
     - auth-service → `8081`
     - user-service → `8082`
     - order-service → `8083`
     - MySQL → `3306`

4. **Verificar que todos los servicios estén corriendo**
   ```bash
   docker compose ps
   ```
   Deberías ver 5 contenedores con estado `Up`.

5. **Acceder a la aplicación**
   - El frontend (React + Vite) debe estar configurado para apuntar a `http://localhost:8080` (el puerto del gateway).
   - También se pueden probar directamente los endpoints con Postman (ver siguiente sección).

Para detener todos los contenedores (sin eliminar los volúmenes de datos):
```bash
docker compose down
```

Para eliminar también los volúmenes (reseteando la base de datos):
```bash
docker compose down -v
```

---

## 🧪 Pruebas con Postman

En la raíz del repositorio se incluye una colección exportada de Postman: `PedidosTracking.postman_collection.json`. Puedes importarla y probar los siguientes flujos. Se recomienda crear un entorno con la variable `base_url = http://localhost:8080`.

### 1. Registro de usuario
- **Endpoint**: `POST {{base_url}}/auth/register`
- **Body** (JSON):
  ```json
  {
    "email": "admin@example.com",
    "password": "123456",
    "nombre": "Admin",
    "apellido": "Principal",
    "rol": "ADMIN"
  }
  ```
- **Respuesta esperada**: `201 Created` o `200 OK` con los datos del usuario (sin contraseña).

### 2. Inicio de sesión (obtener JWT)
- **Endpoint**: `POST {{base_url}}/auth/login`
- **Body**:
  ```json
  {
    "email": "admin@example.com",
    "password": "123456"
  }
  ```
- **Respuesta**: Un objeto con el campo `token`. Copia ese token.
- **Script de prueba** (dentro de la pestaña "Tests" de Postman):
  ```javascript
  if (pm.response.code === 200) {
      pm.environment.set("jwt", pm.response.json().token);
  }
  ```

### 3. Peticiones autenticadas
Para todas las peticiones siguientes, añade el header:
```
Authorization: Bearer {{jwt}}
```

#### Crear un pedido
- `POST {{base_url}}/api/pedidos`
- Body:
  ```json
  {
    "origen": "Calle 123",
    "destino": "Carrera 45",
    "descripcion": "Laptop Dell",
    "clienteId": 1
  }
  ```
- Respuesta: `201 Created` con el objeto `Pedido` (incluyendo el `id` generado). Guarda el `id` para futuras peticiones.

#### Listar pedidos
- `GET {{base_url}}/api/pedidos`
- Respuesta: Array de pedidos (con paginación si se implementa).

#### Cambiar estado del pedido
- `PUT {{base_url}}/api/pedidos/{id}/estado?nuevoEstado=ASIGNADO`
- Respuesta: El pedido actualizado.

#### Asignar repartidor
- `PUT {{base_url}}/api/pedidos/{id}/asignar?repartidorId=2`
- Respuesta: El pedido con el repartidor asignado.

#### Consultar historial del pedido
- `GET {{base_url}}/api/pedidos/{id}/historial`
- Respuesta: Lista de eventos (fecha, tipo, ubicación, etc.).

### 4. Pruebas de error (manejo de excepciones)
- Acceder a un endpoint protegido sin token o con token inválido → `401 Unauthorized`.
- Usuario con rol `REPARTIDOR` tratando de eliminar un pedido → `403 Forbidden`.
- Pedido inexistente → `404 Not Found`.
- Dos operadores editando el mismo pedido simultáneamente → `409 Conflict` (por optimistic locking).

> **Medición de tiempos**: Verifica que cada respuesta llegue en menos de 2 segundos (requerimiento no funcional RNF4). En Postman, el tiempo de respuesta aparece junto al código de estado (p.ej. `200 OK (234 ms)`).

---

## ☁️ Despliegue en Render

Render soporta `docker-compose.yml` de forma nativa (Blueprints). Los pasos para desplegar el sistema en la nube son:

1. Crear una cuenta en [Render.com](https://render.com).
2. Conectar el repositorio de GitHub (el que contiene todo el backend).
3. Crear un nuevo **Blueprint** (Web Service) y seleccionar el repositorio.
4. Render detectará automáticamente el `docker-compose.yml` y mostrará los servicios definidos.
5. Para cada servicio, se pueden configurar **variables de entorno** (necesarias para el perfil `docker`). Como mínimo:
   - `DB_USER=root`
   - `DB_PASSWORD=rootpass` (cambiar por una contraseña segura)
   - `JWT_SECRET` (clave larga y aleatoria, ej. usando `openssl rand -base64 32`)
6. Si se desea una base de datos persistente, se puede:
   - Usar el servicio **Render MySQL** (pago) o
   - Crear una base de datos externa gratuita (por ejemplo, Clever Cloud, Railway) y cambiar la variable `SPRING_DATASOURCE_URL` en cada servicio.
7. Render levantará los contenedores y asignará una URL pública (ej. `https://tu-app.onrender.com`).
8. Actualizar el frontend (React) para que apunte a esa URL en lugar de `localhost`.

> **Nota**: Los volúmenes definidos en `docker-compose.yml` no son persistentes en Render a menos que se configure un servicio de almacenamiento externo. Para un proyecto académico, se puede aceptar que los datos se reseteen en cada despliegue, o se puede usar una base de datos externa.

---

## 📐 Diagrama de arquitectura

A continuación se muestra el diagrama de componentes y flujo de comunicación (usando Mermaid, también se puede incluir una imagen en la carpeta `/docs`):

```mermaid
graph TD
    Client[Cliente React] -->|HTTPS| Gateway[API Gateway :8080]
    Gateway -->|/auth/**| Auth[auth-service :8081]
    Gateway -->|/api/usuarios/**| User[user-service :8082]
    Gateway -->|/api/pedidos/**| Order[order-service :8083]
    Auth --> DB[(MySQL :3306)]
    User --> DB
    Order --> DB
```

La base de datos es compartida, pero cada servicio solo modifica sus propias tablas:
- `auth-service` → tabla `personas` (solo lectura para validar credenciales y escritura para registro).
- `user-service` → tabla `personas` (gestión completa).
- `order-service` → tablas `pedido`, `historial_movimiento`, `ubicacion`.

---

## 👥 Contribuciones y flujo de trabajo en GitHub

Para mantener la calidad del código y evitar errores, se ha configurado la protección de la rama `main` mediante **Rulesets**:

- **No se permite push directo** a `main`. Cualquier intento recibe un error `GH013`.
- Todos los cambios deben realizarse a través de **Pull Requests** (PRs).
- Cada PR necesita al menos **1 aprobación** de otro miembro del equipo antes de fusionarse.
- Se exige que las conversaciones estén resueltas antes del merge.

**Flujo de trabajo recomendado**:

1. Asegurarse de tener la última versión de `main`:
   ```bash
   git checkout main
   git pull origin main
   ```
2. Crear una rama descriptiva:
   ```bash
   git checkout -b feature/agregar-endpoint-pedidos
   ```
3. Hacer commits regulares y subir la rama:
   ```bash
   git push --set-upstream origin feature/agregar-endpoint-pedidos
   ```
4. Abrir un Pull Request en GitHub, asignar al menos un revisor y esperar su aprobación.
5. Después de la aprobación y de pasar los chequeos (si existen), fusionar el PR (preferiblemente con "Squash and merge" para mantener el historial limpio).
6. Eliminar la rama remota y local.

Este flujo garantiza que todo el código que llega a `main` ha sido revisado por al menos otro par de ojos, reduciendo errores y mejorando la calidad.

---

## 📸 Evidencias de pruebas

En la carpeta `/docs` se encuentran capturas de pantalla de las pruebas realizadas con Postman:

- `postman-registro.png` – Registro exitoso de usuario.
- `postman-login.png` – Login con token devuelto.
- `postman-crear-pedido.png` – Creación de pedido con status 201.
- `postman-listar-pedidos.png` – Listado de pedidos con tiempos de respuesta <2s.
- `postman-error-401.png` – Intento de acceso sin token (401).
- `postman-error-409.png` – Conflicto por optimistic locking (cuando dos operadores editan el mismo pedido).

Además, se incluye el archivo de la colección exportada: `PedidosTracking.postman_collection.json`. Para importarlo en Postman, usar "Importar" → elegir el archivo.

---

## 🧠 Buenas prácticas aplicadas

- **Separación de responsabilidades** – Cada microservicio tiene un dominio claro y acotado.
- **API Gateway** – Oculta la topología interna, simplifica el cliente y permite aplicar políticas centralizadas (CORS, caché, timeouts).
- **Autenticación stateless con JWT** – Cada servicio valida el token localmente, sin sesiones en el backend.
- **Optimistic locking** (`@Version`) – Evita pérdidas de actualizaciones en la entidad `Pedido` cuando dos operadores intentan modificarla al mismo tiempo.
- **Contenerización con Docker Compose** – Entorno reproducible idéntico en desarrollo, pruebas y producción.
- **Manejo global de excepciones** – `@RestControllerAdvice` devuelve respuestas HTTP limpias y legibles (400, 401, 404, 409, 500).
- **Uso de DTOs** – Se evita exponer las entidades JPA directamente, previniendo problemas de serialización y mejorando la seguridad.
- **Perfiles de Spring** – `application.yml` para desarrollo local, `application-docker.yml` para contenedores, facilitando la configuración.

---

## 📄 Licencia

Este proyecto es de uso académico como parte de las asignaturas de Ingeniería Web (Universidad Militar Nueva Granada). Puede ser utilizado como base para proyectos similares, respetando los créditos correspondientes.

---

## ✒️ Autores

- Jorge Enrique Celis Cortés
- Liner Fabian Candia Marin
- Miguel Eduardo Parra Amador
- Santiago Andres Diaz Peña