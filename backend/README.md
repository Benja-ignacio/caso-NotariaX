# Notix Backend — Fase 2 EP4

Microservicios Spring Boot del caso Notix. **No procesan trámites reales**: usan datos ficticios y contraseñas en texto plano para la demo académica.

| Servicio | Puerto | Base H2 (archivo) | Responsabilidad |
|---|---|---|---|
| `ms-usuarios` | `8081` | `ms-usuarios/data/msusuarios` | Registro, login y CRUD de usuarios |
| `ms-tramites` | `8082` | `ms-tramites/data/mstramites` | Trámites, documentos, historial y avisos |

> La app Android **todavía no consume estos servicios**. Sigue usando Room como fuente de verdad hasta implementar Retrofit en la Fase 3.

## Requisitos

- JDK 21
- Maven Wrapper incluido (`./mvnw`); no requiere Maven instalado
- Puertos `8081` y `8082` libres

## Ejecutar

Desde `backend/`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk   # ajustar según el entorno
./mvnw test                                          # pruebas de ambos servicios
./mvnw spring-boot:run -pl ms-usuarios               # terminal 1
./mvnw spring-boot:run -pl ms-tramites               # terminal 2
```

También se pueden generar y ejecutar los JAR:

```bash
./mvnw -DskipTests package
java -jar ms-usuarios/target/ms-usuarios-0.0.1-SNAPSHOT.jar
java -jar ms-tramites/target/ms-tramites-0.0.1-SNAPSHOT.jar
```

Las bases H2 se crean en `ms-usuarios/data/` y `ms-tramites/data/`; están ignoradas por Git. Para reiniciar la demo, detener los servicios y borrar esas carpetas.

Consola H2 (solo local): `http://localhost:8081/h2-console` y `http://localhost:8082/h2-console`.

## Usuarios demo

| ID | Correo | Contraseña | Tipo |
|---|---|---|---|
| `USR-001` | `ana.demo@notariax.test` | `demo123` | `PERSONA` |
| `USR-NOTARIO-01` | `notario.demo@notariax.test` | `notario123` | `NOTARIO` |

## Endpoints

### ms-usuarios

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/usuarios` | Lista usuarios (sin contraseña) |
| `GET` | `/api/usuarios/{id}` | Obtiene un usuario |
| `POST` | `/api/usuarios/registro` | Registra y devuelve `201` |
| `POST` | `/api/usuarios/login` | Valida correo, clave y tipo de cuenta |
| `PUT` | `/api/usuarios/{id}` | Actualiza un usuario |
| `DELETE` | `/api/usuarios/{id}` | Elimina un usuario (`204`) |

### ms-tramites

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/tramites` | Todos los trámites, ordenados por fecha |
| `GET` | `/api/tramites/solicitante/{id}` | Trámites del solicitante |
| `GET` | `/api/tramites/{id}` | Detalle con documentos e historial |
| `POST` | `/api/tramites` | Crea un trámite en estado `ENVIADO` |
| `PUT` | `/api/tramites/{id}/estado` | Aprueba o devuelve |
| `PUT` | `/api/tramites/{id}/reenvio` | Corrige y reenvía un `DEVUELTO` |
| `DELETE` | `/api/tramites/{id}` | Elimina un trámite no finalizado |
| `GET` | `/api/avisos/{usuarioId}` | Avisos del usuario |
| `PUT` | `/api/avisos/{id}/leido` | Marca un aviso como leído |

### Reglas de estado

| Estado actual | Acción | Resultado |
|---|---|---|
| `ENVIADO` | Aprobar con `legibilidadConfirmada=true` | `FINALIZADO` |
| `ENVIADO` | Devolver con observación | `DEVUELTO` |
| `DEVUELTO` | Reenviar con antecedentes y documentos | `ENVIADO` |
| `FINALIZADO` | Cualquier cambio o eliminación | `409 Conflict` |

El backend genera el historial y los avisos. El cliente no debe enviarlos para evitar duplicados.

## Ejemplos rápidos

```bash
curl http://localhost:8081/api/usuarios

curl -X POST http://localhost:8081/api/usuarios/login \
  -H 'Content-Type: application/json' \
  -d '{"tipoCuenta":"PERSONA","correo":"ana.demo@notariax.test","contrasena":"demo123"}'

curl http://localhost:8082/api/tramites
curl http://localhost:8082/api/avisos/USR-001
```

## Pruebas

`./mvnw test` ejecuta 11 pruebas: contexto de ambos servicios y flujos HTTP de registro, login, creación, aprobación con legibilidad, devolución, reenvío, eliminación y avisos.

## Estructura

```text
backend/
├── pom.xml                 # proyecto Maven padre
├── mvnw / mvnw.cmd
├── ms-usuarios/
│   └── src/main/java/com/duoc/notix/usuarios/
└── ms-tramites/
    └── src/main/java/com/duoc/notix/tramites/
```

## Limitaciones académicas

- Contraseñas en texto plano, sin JWT ni sesión distribuida.
- Sin comunicación entre microservicios: `ms-tramites` referencia a `USR-001` de forma lógica, sin FK remota.
- Sin CORS ni autenticación de endpoints; el foco es la demo local.
- H2 en archivo, no PostgreSQL/MySQL.
- La app aún no está conectada; eso corresponde a la Fase 3.
