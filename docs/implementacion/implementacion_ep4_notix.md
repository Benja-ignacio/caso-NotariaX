# Guía de implementación EP4 — Notix

> **Proyecto:** Notix (Caso NotariaX)  
> **Asignatura:** DSY1105 — Desarrollo de Aplicaciones Móviles  
> **Entrega:** Evaluación Parcial 4  
> **Base analizada:** código real en `Notix/`, plan `plan_de_desarrollo_ep4_notix.md` y pauta `Ev. Parcial 4_Encargo_Estudiante_ DSY1105.pdf`
> **Objetivo de este documento:** convertir el plan EP4 en una lista técnica de cambios concretos, con archivos, contratos, decisiones de arquitectura, pruebas y criterios de aceptación.

---

## 0. Cómo usar esta guía

Este documento no reemplaza el plan: lo aterriza. El orden recomendado de lectura es:

1. Sección 1 y 2: entender qué falta y cuánto pesa en la rúbrica.
2. Sección 3: validar la arquitectura objetivo.
4. Secciones 4 a 7: implementar backend y capa remota.
5. Sección 8: cerrar los flujos de UI que exige la pauta.
6. Secciones 9 a 12: datos, pruebas, APK y entrega.
7. Secciones 13 a 16: cronograma, checklist, riesgos y mapa de archivos.

### Decisiones que conviene tomar antes de escribir código

| Decisión | Recomendación | Motivo |
|---|---|---|
| API externa | **Mindicador.cl** (`GET https://mindicador.cl/api`) | Aporta UF/UTM reales, útil para aranceles notariales, sin API Key y con respuesta simple. |
| Base de datos backend | **H2 en archivo** (`jdbc:h2:file:...;AUTO_SERVER=TRUE`) | Cero instalación, persiste entre reinicios y permite mostrar consola H2 en la defensa. |
| Lenguaje backend | **Java 21 + Spring Boot 3.5.x** | Es el stack más documentado para la pauta y evita fricción con Kotlin/Spring. |
| Estrategia de datos en la app | **Remoto primero + Room como caché/sesión** | Cumple “envío, recepción y actualización” sin perder la base local que ya funciona. |
| Sesión | **SharedPreferences** (`SessionManager`) | Menos dependencias y tiempo que DataStore; suficiente para logout y persistencia básica. |
| Migración Room | Subir a versión 3 con `MIGRATION_2_3` explícita y conservar `fallbackToDestructiveMigration()` como respaldo | Room sigue siendo la fuente de verdad mientras no exista backend; borrar la base local perdería trámites de la demo. |
| Navegación | Mantener el `enum Pantalla` de `MainActivity` | Cambiar a Navigation Compose no suma puntaje directo y consume tiempo. |

---

## 1. Auditoría del estado actual

### 1.1 Lo que ya está implementado

| Área | Estado real | Evidencia |
|---|---|---|
| Proyecto Android + Compose | Funcional | `Notix/app/build.gradle.kts` |
| Room local | Funcional, versión 3 con migración 2 → 3 | `data/local/NotixDatabase.kt` |
| Login/registro local | Funcional | `repository/AuthRepository.kt`, `viewmodel/LoginViewModel.kt`, `viewmodel/AuthViewmodel.kt` |
| Catálogo y creación de trámites | Funcional | `ui/screens/CatalogoFormatosScreen.kt`, `ui/screens/CrearTramiteScreen.kt` |
| Bandeja del notario | Funcional | `ui/screens/BandejaNotarioScreen.kt` |
| Aprobar / devolver con legibilidad obligatoria | Funcional | `BandejaNotarioViewModel.kt`, `TramiteRepository.kt` |
| Firma simulada por `TipoFirma` | Funcional y obligatoria | `ui/components/FirmaSimuladaDialog.kt` |
| Corregir y reenviar tras devolución | Funcional | `ui/components/CorreccionTramiteDialog.kt`, `TramiteRepository.kt` |
| Historial y avisos | Funcional en Room | `TramiteRepository.kt`, `data/local/Entities.kt` |
| Detalle del trámite para persona | Funcional como diálogo | `MisTramitesScreen.kt` |
| Logout Persona, Empresa y Notario | Funcional | `MainActivity.kt`, `MisTramitesScreen.kt`, `AvisosScreen.kt` |
| Selector nativo de archivos | Funcional, solo metadatos | `CrearTramiteScreen.kt` |

### 1.2 Brechas frente a la EP4

| Brecha | Estado actual | Impacto |
|---|---|---|
| Microservicios Spring Boot | No existen | IE 3.1.2 = 0 |
| Retrofit contra backend | No implementado | IE 3.1.3 = 0 |
| API externa | No implementada | IE 3.1.4 = 0 |
| DELETE desde la UI | No existe | CRUD incompleto en IE 3.1.3 |
| Pruebas unitarias | Solo `ExampleUnitTest` | IE 3.2.1 = 0 |
| APK firmado | Sin `signingConfigs` ni `.jks` | IE 3.3.1 = 0 |
| README con 6 apartados | README existe, pero no cumple los 6 puntos EP4 | IE 3.3.2 parcial |
| Trello / commits de ambos | No evidenciado en el repo | IE 3.3.2 parcial |

### 1.3 Deuda técnica que bloquea la EP4

Estos puntos no están explícitos en el plan, pero si no se corrigen, la integración y las pruebas van a fallar:

1. **`AuthRepository` depende de Android.**
   - Archivo: `Notix/app/src/main/java/com/duoc/notix/repository/AuthRepository.kt`.
   - Usa `android.database.sqlite.SQLiteConstraintException`.
   - En pruebas JVM unitarias eso obliga a stubs de Android o a `unitTests.isReturnDefaultValues = true`, lo que puede ocultar errores.
   - **Acción:** reemplazar por una excepción propia (`DuplicadoException`) o capturar `Exception` y mapear según el origen.

2. **`LoginViewModel` y `AuthViewModel` construyen el repositorio internamente.**
   - Archivos: `viewmodel/LoginViewModel.kt`, `viewmodel/AuthViewmodel.kt`.
   - Hoy extienden `AndroidViewModel` y hacen `NotixDatabase.getDatabase(app)`.
   - **Acción:** convertirlos a `ViewModel` con inyección por constructor y un `factory(context)` en el companion. Sin esto, los tests de login/registro requieren `Application` y Room reales.

3. **Los modelos están en una ruta distinta a su paquete.**
   - Ruta física: `Notix/app/src/main/java/com/duoc/notix/model/`.
   - Paquete declarado: `com.duoc.notix.data.model`.
   - **Acción:** no renombrar a mitad de la entrega; documentar la decisión y usar siempre el import `com.duoc.notix.data.model.*`. Si se mueve, hacerlo en un commit aislado y compilar.

4. **`AuthRepository` está en el paquete `com.duoc.notix.data.repository`, pero `TramiteRepository` está en `com.duoc.notix.repository`.**
   - **Acción:** mantener ambos imports o unificar en `com.duoc.notix.data.repository` en un commit separado.

5. **`MainActivity` no maneja sesión persistida ni botón atrás.**
   - `usuarioId` parte en `"USR-001"`.
   - `onVerTramite` está vacío.
   - **Acción pendiente:** `SessionManager`, `BackHandler` y logout persistente. El logout en memoria ya está conectado para los tres perfiles.

6. **Los IDs de trámites se generan con `(1000..9999).random()`.**
   - Pueden colisionar.
   - **Acción:** que el backend genere el ID y que la app use el ID devuelto.

7. **El seeder local puede competir con el backend.**
   - `DatabaseSeeder.seedIfEmpty()` inserta usuarios y trámites locales al iniciar.
   - **Acción:** el backend es la fuente de verdad; el seeder local queda solo como respaldo de caché y debe usar los mismos IDs/correos demo.

8. **No hay campo de fecha de creación en `Tramite`.**
   - Se ordena por `id` descendente.
   - **Acción:** agregar `fechaCreacion` y `fechaActualizacion` en Room y en el DTO del backend. La evidencia de firma ya existe.

9. ~~`CrearTramiteScreen` inicializa `legibilidadConfirmada = true`.~~ **Resuelto:** inicia en `false` y exige la declaración.

10. ~~`RevisionTramiteDialog` permite aprobar trámites `FINALIZADO` y no exige checkbox.~~ **Resuelto:** checkbox obligatorio, bloqueo por estado y validación en repositorio.

---

## 2. Matriz rúbrica → acción concreta

| Indicador | Peso | Qué debe existir para “muy buen desempeño” | Acción en este proyecto |
|---|---:|---|---|
| **IE 3.1.1** App con todas las pantallas y flujos | 15 % | Flujos completos, sin errores de navegación/ejecución | Firma simulada, corregir/reenviar, legibilidad obligatoria, logout Persona/Empresa, cancelar trámite, botón atrás. |
| **IE 3.1.2** Microservicios con BD y endpoints | 15 % | Todos los microservicios, BD operativa, endpoints coherentes | `ms-usuarios:8081` y `ms-tramites:8082`, H2 en archivo, CRUD, seeder, manejo de errores. |
| **IE 3.1.3** Integración app ↔ microservicios | 15 % | CRUD completo desde la interfaz, sin errores | Retrofit, DTOs, repositorios remotos, sesión, refresco, manejo de errores de red. |
| **IE 3.1.4** API externa integrada visualmente | 15 % | Consumo correcto y visible, sin interferir con lo propio | Mindicador en `CatalogoFormatosScreen`, `runCatching`, tarjeta con UF y fallback. |
| **IE 3.2.1** Pruebas unitarias ≥ 80 % | 15 % | JUnit/MockK, funcionales, cobertura verificable | Suites de `RutValidator`, repositorios y ViewModels; evidencia con cobertura. |
| **IE 3.3.1** APK firmado | 10 % | `.jks`, `signingConfigs`, APK funcional | KeyStore, configuración Gradle, `assembleRelease`, verificación con `apksigner`. |
| **IE 3.3.2** GitHub/Trello/colaboración | 15 % | Commits técnicos de ambos, tareas distribuidas | Ramas, commits por integrante en app y backend, tablero Trello, README de 6 puntos. |

> Regla crítica del PDF: si el proyecto no se ejecuta en la defensa, los ítems posteriores se califican con 0. La prioridad absoluta es que `assembleDebug` y los dos microservicios arranquen sin errores antes de agregar extras.

---

## 3. Arquitectura objetivo

```text
┌────────────────────────────────────────────────────────────────────────────┐
│                     APP ANDROID — NOTIX (Kotlin + Compose)                 │
│                                                                            │
│  [Screens] ──▶ [ViewModels] ──▶ [Repositories]                             │
│                                      │                                     │
│           ┌──────────────────────────┼──────────────────────────┐          │
│           ▼                          ▼                          ▼          │
│  ┌─────────────────┐      ┌─────────────────────┐     ┌──────────────────┐ │
│  │ Room (caché)     │      │ Retrofit Backend    │     │ Retrofit Externa │ │
│  │ sesión + espejo  │      │ ms-usuarios:8081    │     │ Mindicador       │ │
│  │ de datos         │      │ ms-tramites:8082    │     │ (UF/UTM)         │ │
│  └─────────────────┘      └──────────┬──────────┘     └────────┬─────────┘ │
└───────────────────────────────────────┼─────────────────────────┼──────────┘
                                        │ JSON                    │ HTTPS
                                        ▼                         ▼
                             ┌─────────────────────┐      ┌──────────────────┐
                             │ Spring Boot + H2    │      │ mindicador.cl    │
                             │ ms-usuarios         │      │ GET /api         │
                             │ ms-tramites         │      └──────────────────┘
                             └─────────────────────┘
```

### 3.1 Decisión: remoto primero, Room como caché

No conviene que la app siga escribiendo solo en Room y “luego sincronice”, porque la pauta exige demostrar que los datos viajan al backend en tiempo real. La estrategia recomendada es:

1. La UI sigue observando `Flow` de Room (así no hay que reescribir las pantallas).
2. Cada operación CRUD llama primero a Retrofit.
3. Si el backend responde OK, el repositorio escribe/actualiza Room.
4. Si el backend falla, la UI muestra un error y conserva la última caché.
5. Al entrar a una pantalla o luego de una acción, se llama a `refresh...()` para traer cambios del backend.

Esto permite:
- Demostrar CRUD real contra microservicios.
- Mantener la app usable si se cae la red.
- No romper los `Flow` actuales.
- Cumplir “envío, recepción y actualización de datos”.

### 3.2 Decisión: la API externa nunca bloquea

La API externa se encapsula en su propio `RetrofitClient`, su propio repositorio y su propio ViewModel. Cualquier error debe devolver `null` y la UI debe mostrar un texto de respaldo. Nunca debe impedir crear, enviar o revisar un trámite.

---

## 4. Backend: microservicios Spring Boot

### 4.1 Estructura recomendada del repositorio

```text
caso-NotariaX/
├── backend/
│   ├── ms-usuarios/
│   │   ├── pom.xml
│   │   └── src/main/java/com/duoc/notix/usuarios/
│   │       ├── MsUsuariosApplication.java
│   │       ├── controller/UsuarioController.java
│   │       ├── service/UsuarioService.java
│   │       ├── repository/UsuarioRepository.java
│   │       ├── model/Usuario.java
│   │       ├── dto/...
│   │       ├── config/DataSeeder.java
│   │       └── error/GlobalExceptionHandler.java
│   └── ms-tramites/
│       ├── pom.xml
│       └── src/main/java/com/duoc/notix/tramites/
│           ├── MsTramitesApplication.java
│           ├── controller/...
│           ├── service/TramiteService.java
│           ├── repository/...
│           ├── model/Tramite.java, Documento.java, EventoHistorial.java, Aviso.java
│           ├── dto/...
│           ├── config/DataSeeder.java
│           └── error/GlobalExceptionHandler.java
├── docs/
├── Notix/
└── README.md
```

Cada microservicio es un proyecto Gradle/Maven independiente. Se pueden ejecutar por separado y tener su propia base H2.

### 4.2 Dependencias mínimas (Maven)

Para ambos `pom.xml`:

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.5.6</version>
  <relativePath/>
</parent>

<properties>
  <java.version>21</java.version>
</properties>

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

> Si el equipo prefiere Gradle, usar el wrapper de Spring Initializr. Lo importante es que cada microservicio tenga su propio `pom.xml` o `build.gradle` y se pueda ejecutar con un comando claro en el README.

### 4.3 `ms-usuarios` — puerto 8081

**Configuración (`application.properties`)**

```properties
server.port=8081
server.address=0.0.0.0

spring.datasource.url=jdbc:h2:file:./data/msusuarios;AUTO_SERVER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**Entidad `Usuario`**

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `String` PK | `USR-...` |
| `tipoCuenta` | `String` / enum | `PERSONA`, `EMPRESA`, `NOTARIO` |
| `nombreCompleto` | `String` | En empresa, representante legal |
| `correo` | `String` unique | Normalizar a minúsculas |
| `contrasena` | `String` | Texto plano solo demo; documentar |
| `run` | `String` | Limpio |
| `numeroDocumento` | `String` | Limpio |
| `telefono` | `String` | Limpio |
| `comuna` | `String` | — |
| `razonSocialEmpresa` | `String?` | Solo empresa |
| `rutEmpresa` | `String?` | Solo empresa |
| `giroEmpresa` | `String?` | Solo empresa |
| `numeroNotaria` | `Integer?` | Solo notario |

**Endpoints**

| Método | Ruta | Body | Respuesta | Errores |
|---|---|---|---|---|
| `POST` | `/api/usuarios/registro` | `RegistroUsuarioRequest` | `201` + `UsuarioResponse` | `409` correo duplicado, `400` validación |
| `POST` | `/api/usuarios/login` | `LoginRequest` | `200` + `UsuarioResponse` | `401` credenciales, `403` tipo incorrecto, `404` no existe |
| `GET` | `/api/usuarios/{id}` | — | `200` + `UsuarioResponse` | `404` |
| `GET` | `/api/usuarios` | — | `200` + lista | Opcional, útil para depurar |
| `PUT` | `/api/usuarios/{id}` | `ActualizarUsuarioRequest` | `200` + `UsuarioResponse` | Opcional |
| `DELETE` | `/api/usuarios/{id}` | — | `204` | Opcional |

**Contrato de registro**

```json
{
  "id": "USR-001",
  "tipoCuenta": "PERSONA",
  "nombreCompleto": "Ana Demo Prueba",
  "correo": "ana.demo@notariax.test",
  "contrasena": "demo123",
  "run": "111111111",
  "numeroDocumento": "123456789",
  "telefono": "912345678",
  "comuna": "Ñuñoa",
  "razonSocialEmpresa": null,
  "rutEmpresa": null,
  "giroEmpresa": null,
  "numeroNotaria": null
}
```

**Contrato de login**

```json
{
  "tipoCuenta": "PERSONA",
  "correo": "ana.demo@notariax.test",
  "contrasena": "demo123"
}
```

**Reglas del servicio**

- Normalizar `correo` con `trim().toLowerCase()`.
- Validar `tipoCuenta` contra la cuenta.
- No devolver `contrasena` en `UsuarioResponse`.
- Si se implementa hash, usar BCrypt; para la demo se puede dejar texto plano y documentarlo como limitación académica.
- El `DataSeeder` debe crear:
  - `USR-001` / `ana.demo@notariax.test` / `demo123` / Persona.
  - `USR-NOTARIO-01` / `notario.demo@notariax.test` / `notario123` / Notario.
  - Una cuenta de empresa opcional para la demo.

### 4.4 `ms-tramites` — puerto 8082

**Configuración (`application.properties`)**

```properties
server.port=8082
server.address=0.0.0.0

spring.datasource.url=jdbc:h2:file:./data/mstramites;AUTO_SERVER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**Modelo de datos**

`Tramite`:

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `String` PK | Generado por backend, formato `TX-2026-XXXX` |
| `formatoId` | `String` | `F01`, `F02`, `F03` |
| `formatoNombre` | `String` | Denormalizado |
| `formatoCategoria` | `String` | Denormalizado |
| `solicitanteId` | `String` | Referencia lógica a `ms-usuarios`; sin FK entre servicios |
| `solicitanteNombre` | `String` | Copia al crear |
| `actuaEnNombreDe` | `String?` | Empresa |
| `tipoFirma` | `String` | `SIMPLE`, `AVANZADA`, `NOTARIADA` |
| `estado` | `String` | `ENVIADO`, `DEVUELTO`, `FINALIZADO` |
| `antecedentesNombreCompleto` | `String` | — |
| `antecedentesDocumentoIdentidad` | `String` | — |
| `antecedentesCorreoContacto` | `String` | — |
| `antecedentesComuna` | `String` | — |
| `observacion` | `String?` | Motivo de devolución |
| `legibilidadConfirmada` | `Boolean` | Exigido para aprobar |
| `firmaEvidencia` | `String?` | `SIMPLE_CANVAS`, `AVANZADA_TOKEN_SIMULADO`, `NOTARIADA_SIMULADA` |
| `fechaCreacion` | `LocalDateTime` | Orden real |
| `fechaActualizacion` | `LocalDateTime` | — |

`Documento`:

| Campo | Tipo |
|---|---|
| `id` | `Long` autogenerado |
| `tramite` | `@ManyToOne` |
| `nombre` | `String` |
| `tamanoKb` | `Integer` |
| `estado` | `ACEPTADO` / `OBSERVADO` |

`EventoHistorial`:

| Campo | Tipo |
|---|---|
| `id` | `Long` autogenerado |
| `tramite` | `@ManyToOne` |
| `descripcion` | `String` |
| `fechaHora` | `LocalDateTime` |
| `rol` | `PERSONA` / `EMPRESA` / `NOTARIO` |

`Aviso`:

| Campo | Tipo |
|---|---|
| `id` | `String` PK |
| `destinatarioId` | `String` |
| `tramiteId` | `String` |
| `mensaje` | `String` |
| `fechaHora` | `LocalDateTime` |
| `leido` | `Boolean` |

**Endpoints**

| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/api/tramites` | Todos los trámites (bandeja del notario) |
| `GET` | `/api/tramites/solicitante/{id}` | Trámites de una persona/empresa |
| `GET` | `/api/tramites/{id}` | Detalle |
| `POST` | `/api/tramites` | Crear trámite |
| `PUT` | `/api/tramites/{id}/estado` | Aprobar o devolver |
| `PUT` | `/api/tramites/{id}/reenvio` | Corregir y reenviar |
| `DELETE` | `/api/tramites/{id}` | Cancelar/eliminar |
| `GET` | `/api/avisos/{usuarioId}` | Avisos del usuario |
| `PUT` | `/api/avisos/{id}/leido` | Marcar aviso como leído |

> El plan exige como mínimo `GET /api/avisos/{usuarioId}`. Agregar `PUT /api/avisos/{id}/leido` es necesario para que el estado leído viva en el backend y no solo en Room.

**Máquina de estados**

| Estado actual | Acción | Estado nuevo | Efectos obligatorios |
|---|---|---|---|
| `ENVIADO` | Aprobar | `FINALIZADO` | Exigir `legibilidadConfirmada=true`; evento de historial; aviso al solicitante. |
| `ENVIADO` | Devolver | `DEVUELTO` | Exigir `observacion` no vacía; evento; aviso. |
| `DEVUELTO` | Reenviar | `ENVIADO` | Actualizar antecedentes/documentos; limpiar `observacion`; `legibilidadConfirmada=false`; evento; aviso. |
| `Cualquiera no FINALIZADO` | Cancelar | Eliminado | Borrar documentos e historial en cascada; borrar avisos asociados. |
| `FINALIZADO` | Cualquier acción | `409 Conflict` | No permitir aprobar, devolver, reenviar ni cancelar. |

**Regla clave:** el backend debe generar el historial y los avisos. Si el cliente los envía, se duplican y se pierde la consistencia. El cliente solo envía la acción y los datos editables.

**Contrato de creación**

```json
{
  "formatoId": "F01",
  "formatoNombre": "Poder simple",
  "formatoCategoria": "Poderes",
  "solicitanteId": "USR-001",
  "solicitanteNombre": "Ana Demo Prueba",
  "actuaEnNombreDe": null,
  "tipoFirma": "SIMPLE",
  "firmaEvidencia": "SIMPLE_CANVAS",
  "antecedentes": {
    "nombreCompleto": "Ana Demo Prueba",
    "documentoIdentidad": "111111111",
    "correoContacto": "ana.demo@notariax.test",
    "comuna": "Ñuñoa"
  },
  "documentos": [
    { "nombre": "poder_demo.pdf", "tamanoKb": 250, "estado": "ACEPTADO" }
  ]
}
```

**Contrato de cambio de estado**

```json
{
  "estado": "FINALIZADO",
  "observacion": null,
  "legibilidadConfirmada": true,
  "rol": "NOTARIO",
  "comentario": "Documentos legibles y antecedentes consistentes."
}
```

Para devolver:

```json
{
  "estado": "DEVUELTO",
  "observacion": "El anexo está ilegible. Vuelve a cargarlo.",
  "legibilidadConfirmada": false,
  "rol": "NOTARIO",
  "comentario": "Devolución con observaciones."
}
```

**Contrato de reenvío**

```json
{
  "antecedentes": {
    "nombreCompleto": "Ana Demo Prueba",
    "documentoIdentidad": "111111111",
    "correoContacto": "ana.demo@notariax.test",
    "comuna": "Providencia"
  },
  "documentos": [
    { "nombre": "anexo_corregido.pdf", "tamanoKb": 310, "estado": "ACEPTADO" }
  ],
  "comentario": "Se corrigió la comuna y se reemplazó el anexo."
}
```

**Formato de errores**

```json
{
  "timestamp": "2026-11-20T12:00:00",
  "status": 409,
  "error": "CONFLICT",
  "message": "El trámite ya está finalizado."
}
```

La app debe leer `message` para mostrar el error al usuario.

**Seeder de `ms-tramites`**

Debe crear, si la base está vacía:

| ID | Formato | Estado | Solicitante |
|---|---|---|---|
| `TX-2026-0042` | Poder simple | `DEVUELTO` | `USR-001` |
| `TX-2026-0039` | Declaración jurada simple | `ENVIADO` | `USR-001` |
| `TX-2026-0031` | Autorización de viaje | `FINALIZADO` | `USR-001` |
| `TX-2026-0001` | Poder simple | `ENVIADO` | `USR-001` |

Incluir documentos, historial y al menos un aviso `AVS-001`, para que la app tenga datos al iniciar.

### 4.5 Ejecución del backend

```bash
# Terminal 1
cd backend/ms-usuarios
./mvnw spring-boot:run

# Terminal 2
cd backend/ms-tramites
./mvnw spring-boot:run
```

Verificar:

- `http://localhost:8081/api/usuarios/USR-001`
- `http://localhost:8082/api/tramites`
- `http://localhost:8081/h2-console`
- `http://localhost:8082/h2-console`

Si se usa Windows, `mvnw.cmd`. Si no se incluye wrapper, documentar `mvn spring-boot:run`.

---

## 5. Android: dependencias y configuración de red

### 5.1 `Notix/gradle/libs.versions.toml`

Agregar versiones:

```toml
[versions]
retrofit = "2.11.0"
okhttp = "4.12.0"
mockk = "1.13.12"
coroutinesTest = "1.8.1"
```

Agregar librerías:

```toml
[libraries]
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
```

> Si MockK 1.13.12 falla con Kotlin 2.2.10 por metadatos, subir a la última 1.14.x. Verificar con `./gradlew testDebugUnitTest`.

### 5.2 `Notix/app/build.gradle.kts`

Agregar:

```kotlin
android {
    // ...
    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        // ...
        buildConfigField("String", "USUARIOS_BASE_URL", "\"http://10.0.2.2:8081/\"")
        buildConfigField("String", "TRAMITES_BASE_URL", "\"http://10.0.2.2:8082/\"")
        buildConfigField("String", "EXTERNAL_BASE_URL", "\"https://mindicador.cl/\"")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // ...
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

**Sobre `buildConfigField`:** permite cambiar la URL sin tocar código. Para emulador usar `10.0.2.2`. Para teléfono físico, reemplazar por la IP LAN del PC, por ejemplo `http://192.168.1.50:8081/`.

### 5.3 `AndroidManifest.xml`

Agregar antes de `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Agregar dentro de `<application>`:

```xml
android:usesCleartextTraffic="true"
```

**Alternativa más controlada:** crear `res/xml/network_security_config.xml` y referenciarlo con `android:networkSecurityConfig="@xml/network_security_config"`. Para la demo, `usesCleartextTraffic="true"` es suficiente y es lo que pide el plan.

> Advertencia: tráfico HTTP sin cifrar solo es aceptable en un proyecto académico local. Documentarlo en el README.

---

## 6. Android: capa remota

### 6.1 Estructura de paquetes recomendada

```text
Notix/app/src/main/java/com/duoc/notix/
├── data/
│   ├── local/                 # Room actual
│   ├── remote/
│   │   ├── ApiResult.kt
│   │   ├── dto/
│   │   │   ├── UsuarioDto.kt
│   │   │   ├── TramiteDto.kt
│   │   │   ├── AvisoDto.kt
│   │   │   └── Requests.kt
│   │   ├── api/
│   │   │   ├── UsuariosApi.kt
│   │   │   ├── TramitesApi.kt
│   │   │   └── IndicadoresApi.kt
│   │   ├── client/
│   │   │   ├── UsuariosRetrofitClient.kt
│   │   │   ├── TramitesRetrofitClient.kt
│   │   │   └── ExternalRetrofitClient.kt
│   │   └── mapper/
│   │       ├── UsuarioRemoteMapper.kt
│   │       └── TramiteRemoteMapper.kt
│   ├── session/
│   │   └── SessionManager.kt
│   └── validation/
├── repository/
│   ├── AuthRepository.kt
│   ├── TramiteRepository.kt
│   └── IndicadoresRepository.kt
└── viewmodel/
    └── IndicadoresViewModel.kt
```

> No es obligatorio mover los archivos existentes. Lo importante es que la nueva capa remota tenga paquetes claros y que los repositorios sean inyectables.

### 6.2 `ApiResult`

```kotlin
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>
}
```

Uso:

```kotlin
suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: retrofit2.HttpException) {
        ApiResult.Error(parseError(e), e.code())
    } catch (e: java.io.IOException) {
        ApiResult.Error("Sin conexión con el servidor. Se muestra la última información guardada.")
    } catch (e: Exception) {
        ApiResult.Error(e.localizedMessage ?: "Error inesperado.")
    }
```

### 6.3 DTOs

Deben reflejar exactamente los contratos del backend. Ejemplo:

```kotlin
data class UsuarioDto(
    val id: String,
    val tipoCuenta: String,
    val nombreCompleto: String,
    val correo: String,
    val run: String,
    val numeroDocumento: String,
    val telefono: String,
    val comuna: String,
    val razonSocialEmpresa: String? = null,
    val rutEmpresa: String? = null,
    val giroEmpresa: String? = null,
    val numeroNotaria: Int? = null
)

data class LoginRequest(
    val tipoCuenta: String,
    val correo: String,
    val contrasena: String
)

data class RegistroRequest(
    val tipoCuenta: String,
    val nombreCompleto: String,
    val correo: String,
    val contrasena: String,
    val run: String,
    val numeroDocumento: String,
    val telefono: String,
    val comuna: String,
    val razonSocialEmpresa: String? = null,
    val rutEmpresa: String? = null,
    val giroEmpresa: String? = null,
    val numeroNotaria: Int? = null
)

data class TramiteDto(
    val id: String,
    val formatoId: String,
    val formatoNombre: String,
    val formatoCategoria: String,
    val solicitanteId: String,
    val solicitanteNombre: String,
    val actuaEnNombreDe: String? = null,
    val tipoFirma: String,
    val estado: String,
    val antecedentes: AntecedentesDto,
    val documentos: List<DocumentoDto>,
    val observacion: String? = null,
    val legibilidadConfirmada: Boolean = false,
    val firmaEvidencia: String? = null,
    val fechaCreacion: String,
    val historial: List<EventoHistorialDto>
)
```

`UsuarioDto` no incluye `contrasena`. Al mapear a `Usuario` del dominio, usar `contrasena = ""` porque la app ya no valida claves localmente.

### 6.4 Interfaces Retrofit

```kotlin
interface UsuariosApi {
    @POST("api/usuarios/registro")
    suspend fun registrar(@Body request: RegistroRequest): UsuarioDto

    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): UsuarioDto

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: String): UsuarioDto
}

interface TramitesApi {
    @GET("api/tramites")
    suspend fun todos(): List<TramiteDto>

    @GET("api/tramites/solicitante/{id}")
    suspend fun porSolicitante(@Path("id") id: String): List<TramiteDto>

    @GET("api/tramites/{id}")
    suspend fun detalle(@Path("id") id: String): TramiteDto

    @POST("api/tramites")
    suspend fun crear(@Body request: CrearTramiteRequest): TramiteDto

    @PUT("api/tramites/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: String,
        @Body request: CambioEstadoRequest
    ): TramiteDto

    @PUT("api/tramites/{id}/reenvio")
    suspend fun reenviar(
        @Path("id") id: String,
        @Body request: ReenvioRequest
    ): TramiteDto

    @DELETE("api/tramites/{id}")
    suspend fun eliminar(@Path("id") id: String)

    @GET("api/avisos/{usuarioId}")
    suspend fun avisos(@Path("usuarioId") usuarioId: String): List<AvisoDto>

    @PUT("api/avisos/{id}/leido")
    suspend fun marcarAvisoLeido(@Path("id") id: String)
}

interface IndicadoresApi {
    @GET("api")
    suspend fun indicadores(): IndicadorResponse
}
```

### 6.5 Clientes Retrofit

```kotlin
object UsuariosRetrofitClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: UsuariosApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.USUARIOS_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuariosApi::class.java)
    }
}
```

Repetir para `TramitesRetrofitClient` con `BuildConfig.TRAMITES_BASE_URL` y para `ExternalRetrofitClient` con timeouts más cortos (5 s).

### 6.6 API externa: Mindicador

**Respuesta esperada (resumida)**

```json
{
  "version": "1.7.0",
  "autor": "mindicador.cl",
  "fecha": "2026-11-20T12:00:00.000Z",
  "uf": {
    "codigo": "UF",
    "nombre": "Unidad de Fomento",
    "unidad": "CLP",
    "valor": 39450.12,
    "fecha": "2026-11-20T12:00:00.000Z"
  },
  "utm": {
    "codigo": "UTM",
    "nombre": "Unidad Tributaria Mensual",
    "unidad": "CLP",
    "valor": 67500.0,
    "fecha": "2026-11-20T12:00:00.000Z"
  }
}
```

**Modelos**

```kotlin
data class IndicadorResponse(
    val uf: IndicadorDto? = null,
    val utm: IndicadorDto? = null,
    val fecha: String? = null
)

data class IndicadorDto(
    val codigo: String? = null,
    val nombre: String? = null,
    val unidad: String? = null,
    val valor: Double? = null,
    val fecha: String? = null
)
```

**Repositorio aislado**

La llamada debe quedar envuelta en `runCatching` y devolver `null` ante cualquier fallo, tal como pide el plan:

```kotlin
class IndicadoresRepository(
    private val api: IndicadoresApi = ExternalRetrofitClient.api
) {
    suspend fun obtenerUf(): IndicadorDto? = runCatching {
        api.indicadores().uf
    }.getOrNull()
}
```

Si se prefiere conservar el error para mostrarlo:

```kotlin
suspend fun obtenerUfConError(): Result<IndicadorDto?> = runCatching {
    api.indicadores().uf
}
```

En ambos casos la UI debe tratar `null` o `failure` como “indicador no disponible” y permitir continuar.

**ViewModel**

```kotlin
data class IndicadoresUiState(
    val cargando: Boolean = true,
    val uf: Double? = null,
    val fecha: String? = null,
    val error: String? = null
)
```

**UI en `CatalogoFormatosScreen`**

Agregar una tarjeta al inicio de la lista:

- Cargando: `"Consultando valor de la UF..."`.
- Éxito: `"UF hoy: $39.450 CLP — Referencia arancelaria"`.
- Error: `"Indicador no disponible. Puedes continuar con tu trámite."`.

La llamada debe ejecutarse en `LaunchedEffect(Unit)` y no debe bloquear el resto de la pantalla. Si se prefiere, se puede mostrar también en `MisTramitesScreen`, pero basta con una ubicación visible y coherente.

---

## 7. Repositorios, sesión y refresco

### 7.1 `SessionManager`

```kotlin
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("notix_session", Context.MODE_PRIVATE)

    fun guardar(usuarioId: String, tipoCuenta: TipoCuenta) {
        prefs.edit()
            .putString("usuarioId", usuarioId)
            .putString("tipoCuenta", tipoCuenta.name)
            .apply()
    }

    fun usuarioId(): String? = prefs.getString("usuarioId", null)

    fun tipoCuenta(): TipoCuenta? =
        prefs.getString("tipoCuenta", null)?.let {
            runCatching { TipoCuenta.valueOf(it) }.getOrNull()
        }

    fun limpiar() {
        prefs.edit().clear().apply()
    }
}
```

Integración en `MainActivity`:

- Al iniciar, si hay sesión, navegar a `MisTramites` o `BandejaNotario`.
- Al hacer login/registro, guardar sesión.
- Al cerrar sesión, limpiar sesión y volver a `Welcome`.
- Eliminar el valor por defecto `"USR-001"` como usuario activo.

### 7.2 `AuthRepository` remoto

Responsabilidades:

1. Llamar a `UsuariosApi.login` / `registrar`.
2. Mapear `UsuarioDto` a `Usuario`.
3. Guardar el usuario en Room como caché (`upsertUsuario`).
4. Devolver `AuthResult.Success` o `AuthResult.Error`.
5. Mapear errores HTTP:
   - `401` → “Contraseña incorrecta.”
   - `403` → “Esa cuenta no es de tipo X.”
   - `404` → “No existe una cuenta con ese correo.”
   - `409` → “Ya existe una cuenta con ese correo.”
   - `IOException` → “Sin conexión con el servidor.”

No volver a comparar contraseñas localmente. La fuente de verdad es el backend.

### 7.3 `TramiteRepository` remoto + caché

Métodos recomendados:

```kotlin
class TramiteRepository(
    private val tramiteDao: TramiteDao,
    private val avisoDao: AvisoDao,
    private val usuarioDao: UsuarioDao,
    private val tramitesApi: TramitesApi
) {
    fun observarMisTramites(usuarioId: String): Flow<List<Tramite>> // igual que hoy

    suspend fun refrescarMisTramites(usuarioId: String): ApiResult<Unit> {
        return when (val r = safeApiCall { tramitesApi.porSolicitante(usuarioId) }) {
            is ApiResult.Success -> {
                guardarEnCache(r.data)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> r
        }
    }

    suspend fun crearTramite(request: CrearTramiteRequest): ApiResult<Tramite> {
        return when (val r = safeApiCall { tramitesApi.crear(request) }) {
            is ApiResult.Success -> {
                guardarEnCache(listOf(r.data))
                ApiResult.Success(r.data.toDomain())
            }
            is ApiResult.Error -> r
        }
    }

    suspend fun aprobar(tramiteId: String, legibilidad: Boolean): ApiResult<Unit>
    suspend fun devolver(tramiteId: String, observacion: String): ApiResult<Unit>
    suspend fun reenviar(tramiteId: String, request: ReenvioRequest): ApiResult<Unit>
    suspend fun eliminar(tramiteId: String): ApiResult<Unit>
}
```

**Estrategia de caché**

- `guardarEnCache` debe ser transaccional: reemplazar trámite, documentos e historial.
- Para listas, borrar los trámites del solicitante y volver a insertar, o hacer `upsert` por ID y eliminar los que ya no vienen.
- Los avisos se sincronizan con `GET /api/avisos/{usuarioId}`.
- `marcarAvisoLeido` debe llamar primero al backend y luego actualizar Room.

**Refresco**

- En `MisTramitesScreen`: `LaunchedEffect(usuarioId) { vm.refrescar() }`.
- En `BandejaNotarioScreen`: `LaunchedEffect(usuarioId) { vm.refrescar() }`.
- Después de crear, aprobar, devolver, reenviar o eliminar: llamar a `refrescar()`.
- Opcional para “tiempo real” en la defensa: polling cada 10–15 s mientras la pantalla está visible.

```kotlin
LaunchedEffect(usuarioId) {
    while (isActive) {
        vm.refrescar()
        delay(10_000)
    }
}
```

Documentar que no es WebSocket: es actualización periódica vía REST, suficiente para el alcance académico.

### 7.4 Seeder local

Opciones:

- **Recomendada:** dejar `DatabaseSeeder` solo para datos de respaldo y asegurar que los IDs coincidan con el backend.
- Alternativa: eliminar la siembra de trámites locales y dejar solo usuarios demo, porque el backend ya siembra.
- En cualquier caso, no debe ejecutarse si hay conexión y el backend ya respondió, para evitar duplicados.

---

## 8. Cambios de UI y flujos (Fase 1 del plan)

### 8.1 Firma simulada antes de enviar

**Archivos:** `ui/screens/CrearTramiteScreen.kt`, `viewmodel/CrearTramiteViewModel.kt`, `model/Tramite.kt`, `data/local/Entities.kt`, mappers y DTOs.

**Flujo**

1. Usuario completa antecedentes y adjunta documento.
2. Pulsa “Revisar y enviar solicitud”.
3. Se valida el formulario.
4. Se abre el paso de firma según `formato.firmaRequerida`:
   - `SIMPLE`: diálogo con `Canvas` para dibujar un trazo.
   - `AVANZADA`: diálogo que simula e-token/certificado con código de 6 dígitos o botón “Simular e-token”.
   - `NOTARIADA`: diálogo con confirmación de autorización notarial simulada.
5. Solo si la firma se confirma, se abre el modal de resumen.
6. Al confirmar, se guarda `firmaEvidencia` en el trámite.

**Implementación sugerida**

- Crear `ui/components/FirmaSimuladaDialog.kt`.
- Para `SIMPLE`:
  - `var path by remember { mutableStateOf(Path()) }`.
  - `Canvas` con `pointerInput` y `detectDragGestures`.
  - `path.isEmpty` para habilitar “Confirmar firma”.
  - Botón “Limpiar”.
- Para `AVANZADA`:
  - Campo de código o botón “Simular e-token”.
  - Confirmar solo si el código tiene 6 dígitos o se pulsó el botón.
- Para `NOTARIADA`:
  - `Checkbox` “Confirmo la autorización notarial simulada”.
  - Confirmar solo si está marcado.
- Guardar en el ViewModel:

```kotlin
var firmaEvidencia by mutableStateOf<String?>(null)
    private set

fun onFirmaConfirmada(evidencia: String) {
    firmaEvidencia = evidencia
}
```

- En `guardarTramite`, si `firmaEvidencia == null`, mostrar error y no guardar.
- En el resumen y en el detalle mostrar: `"Firma: ${tipoFirma.etiqueta} (simulada)"` y la evidencia.

**Criterio de aceptación:** no se puede enviar ningún trámite sin completar el paso de firma correspondiente.

### 8.2 Corregir y reenviar tras devolución

**Archivos:** `ui/screens/MisTramitesScreen.kt`, `viewmodel/TramitesViewmodel.kt`, `repository/TramiteRepository.kt`.

**UI**

- En `DetalleClienteDialog`, si `tramite.estado == DEVUELTO`, mostrar botón **“Corregir y reenviar”**.
- Abrir `CorreccionTramiteDialog` con:
  - Nombre completo editable.
  - RUN editable.
  - Correo editable.
  - Comuna editable.
  - Botón para adjuntar/reemplazar documento.
  - Campo opcional “Comentario de corrección”.
  - Botones “Cancelar” y “Reenviar al notario”.

**ViewModel**

Agregar a `MisTramitesViewModel`:

```kotlin
fun reenviarTramite(
    tramite: Tramite,
    antecedentes: Antecedentes,
    nuevoDocumento: Documento?,
    comentario: String
)
```

Debe:

1. Llamar a `repo.reenviar(...)`.
2. En éxito, cerrar el diálogo, refrescar y mostrar mensaje.
3. En error, mantener el diálogo y mostrar el mensaje del backend.

**Backend**

`PUT /api/tramites/{id}/reenvio`:

- Validar que el trámite esté `DEVUELTO`.
- Actualizar antecedentes.
- Reemplazar/agregar documentos según el request.
- Cambiar estado a `ENVIADO`.
- Limpiar `observacion`.
- `legibilidadConfirmada = false`.
- Agregar evento `"Corregido y reenviado por el solicitante: $comentario"`.
- Crear aviso al solicitante y/o al notario.

**Criterio de aceptación:** un trámite devuelto puede editarse, reenviarse y vuelve a aparecer en la bandeja del notario como `ENVIADO`, con el evento en el historial.

### 8.3 Blindar el diálogo del notario

**Archivo:** `ui/screens/BandejaNotarioScreen.kt`.

Cambios en `RevisionTramiteDialog`:

1. Agregar estado:

```kotlin
var legibilidadConfirmada by remember { mutableStateOf(false) }
```

2. Agregar `Checkbox`:

```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    Checkbox(
        checked = legibilidadConfirmada,
        onCheckedChange = { legibilidadConfirmada = it }
    )
    Text("Confirmo que los documentos son legibles.")
}
```

3. Deshabilitar “Aprobar”:

```kotlin
Button(
    onClick = { onAprobar(legibilidadConfirmada) },
    enabled = legibilidadConfirmada
) { Text("Aprobar trámite") }
```

4. Si `tramite.estado == FINALIZADO`:
   - No mostrar botones “Aprobar” ni “Devolver”.
   - Mostrar `"Este trámite ya fue finalizado."`.
   - Solo permitir cerrar.
5. Si `tramite.estado == DEVUELTO`:
   - Mostrar `"Esperando corrección del solicitante."`.
   - No permitir aprobar/devolver de nuevo.
6. Si `tramite.estado == ENVIADO`:
   - Mostrar acciones normales.
7. Pasar `legibilidadConfirmada` al ViewModel y persistirlo en el backend.

**ViewModel**

```kotlin
fun aprobarTramite(tramite: Tramite, legibilidadConfirmada: Boolean)
```

**Backend**

Rechazar con `409` si se intenta aprobar sin `legibilidadConfirmada = true` o si el estado no es `ENVIADO`.

**Criterio de aceptación:** no se puede aprobar sin marcar el checkbox; no se puede volver a actuar sobre un trámite finalizado.

### 8.4 Logout Persona/Empresa

**Archivos:** `MainActivity.kt`, `ui/screens/MisTramitesScreen.kt`, `ui/screens/AvisosScreen.kt`, `ui/components/BarraInferiorPersona.kt`.

Cambios:

1. Agregar `onCerrarSesion: () -> Unit` a `MisTramitesScreen` y `AvisosScreen`.
2. Agregar un icono de logout en el encabezado de `MisTramitesScreen` (junto a la campana) o en el ítem “Perfil” de la barra inferior.
3. En `MainActivity`:

```kotlin
fun cerrarSesion() {
    session.limpiar()
    usuarioId = ""
    pantallaActual = Pantalla.Welcome
}
```

4. Cambiar el logout del notario para que también vuelva a `Welcome`, no a `Login`.
5. Confirmar con un diálogo “¿Cerrar sesión?” para evitar pulsaciones accidentales.

**Criterio de aceptación:** desde Persona, Empresa y Notario se puede volver a `WelcomeScreen` y cambiar de rol sin reiniciar la app.

### 8.5 Cancelar/eliminar trámite (CRUD DELETE)

**Motivo:** la pauta exige operaciones CRUD desde la interfaz. Actualmente no hay ninguna acción de borrado.

**UI**

- En `DetalleClienteDialog`, si `estado != FINALIZADO`, mostrar botón **“Cancelar trámite”**.
- Pedir confirmación: “Esta acción eliminará el trámite y su historial.”
- Llamar a `vm.eliminarTramite(tramite.id)`.

**Backend**

`DELETE /api/tramites/{id}`:

- Si `FINALIZADO`, responder `409`.
- Borrar documentos e historial en cascada.
- Borrar avisos asociados.
- Responder `204`.

**Criterio de aceptación:** se puede eliminar un trámite no finalizado y desaparece de “Mis trámites” y de la bandeja del notario.

### 8.6 Tarjeta de API externa

**Archivo:** `ui/screens/CatalogoFormatosScreen.kt` + `viewmodel/IndicadoresViewModel.kt` + `repository/IndicadoresRepository.kt`.

- Agregar la tarjeta al inicio de la `LazyColumn`.
- Mostrar `UF` y, si se quiere, `UTM`.
- Formatear en CLP:

```kotlin
val formato = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
Text("UF hoy: ${formato.format(uf)}")
```

- Si `uf == null`, mostrar el texto de respaldo y permitir continuar.
- No mostrar errores técnicos ni stack traces.

### 8.7 Navegación y botón atrás

- Agregar `BackHandler` en `MainActivity` para que el botón atrás del sistema no cierre la app desde pantallas internas.
- Mapear:
  - `Login` → `Welcome`
  - `CrearCuenta` → `Welcome`
  - `MisTramites` → `Welcome` (o salir con confirmación)
  - `CatalogoFormatos` → `MisTramites`
  - `CrearTramite` → `CatalogoFormatos`
  - `Avisos` → `MisTramites`
  - `BandejaNotario` → `Welcome`
- Corregir el efecto secundario en composición:

```kotlin
formatoSeleccionado?.let { ... } ?: run { pantallaActual = Pantalla.CatalogoFormatos }
```

Ese `run` cambia estado durante la composición. Moverlo a un `LaunchedEffect` o validar antes de navegar.

---

## 9. Modelo de datos: cambios en Room v3

### 9.1 Campos nuevos

| Campo | Dónde | Tipo | Estado |
|---|---|---|---|
| `firmaEvidencia` | `Tramite` / `TramiteEntity` | `String?` | Implementado en Room v3. |
| `fechaCreacion` | `Tramite` / `TramiteEntity` | `String` | Pendiente; hoy se ordena por ID. |
| `fechaActualizacion` | `Tramite` / `TramiteEntity` | `String?` | Pendiente; los eventos de historial ya tienen fecha. |

Opcional:

| Campo | Motivo |
|---|---|
| `remoteId` | Solo si se decide separar ID local de ID remoto. No recomendado para esta entrega. |

### 9.2 Migración

- La versión 3 ya está aplicada con `MIGRATION_2_3` explícita:
  ```sql
  ALTER TABLE tramites ADD COLUMN firmaEvidencia TEXT
  ```
- Esta migración conserva usuarios, trámites, documentos, historial y avisos.
- `fallbackToDestructiveMigration()` se mantiene solo como respaldo para versiones futuras sin ruta definida.
- Si más adelante Room pasa a ser caché del backend, se podrá volver a evaluar la estrategia destructiva.
- Para la EP4 no es necesario mover la evidencia de firma a un formato más complejo: sigue siendo una simulación.

### 9.3 Mapeo

Actualizar:

- `data/local/Mappers.kt`
- `data/local/Entities.kt`
- `data/local/TypeConverters.kt` si se agregan enums nuevos.
- `model/Tramite.kt`
- DTOs y mappers remotos.

**Cuidado:** el paquete real de los modelos es `com.duoc.notix.data.model`, aunque la carpeta se llame `model/`. No cambiar los imports a `com.duoc.notix.model` sin mover los archivos.

---

## 10. Pruebas unitarias y cobertura ≥ 80 %

### 10.1 Dependencias

Ya agregadas en la sección 5:

```kotlin
testImplementation(libs.junit)
testImplementation(libs.mockk)
testImplementation(libs.kotlinx.coroutines.test)
```

### 10.2 Refactorizaciones necesarias para poder testear

Sin estos cambios, la cobertura será artificial o los tests fallarán:

1. **Inyectar repositorios en los ViewModels.**
   - `LoginViewModel(repo: AuthRepository)`.
   - `AuthViewModel(repo: AuthRepository)`.
   - `MisTramitesViewModel(repo: TramiteRepository, usuarioId: String)` ya es inyectable.
   - `BandejaNotarioViewModel` ya es inyectable.
   - `CrearTramiteViewModel` ya es inyectable.
2. **Eliminar `AndroidViewModel` de login/registro.**
   - Crear `companion object { fun factory(context: Context) = viewModelFactory { initializer { ... } } }`.
3. **Quitar `SQLiteConstraintException` de `AuthRepository`.**
   - Usar `AuthResult.Error("...")` a partir de `ApiResult.Error`.
4. **Abstraer la capa remota detrás de interfaces.**
   - `UsuariosRemoteDataSource`, `TramitesRemoteDataSource`, `IndicadoresRemoteDataSource`.
   - Los repositorios dependen de las interfaces; los tests usan fakes o MockK.
5. **No usar `Dispatchers.Main` real en tests.**
   - Agregar `MainDispatcherRule`.

```kotlin
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### 10.3 Suites mínimas

| Suite | Casos sugeridos | Qué valida |
|---|---|---|
| `RutValidatorTest` | RUN válido, inválido, vacío, muy corto, letras, dígito `K`, `calcularDv`, formateo | Validación pura |
| `AuthRepositoryTest` | Login OK, clave incorrecta, rol incorrecto, correo inexistente, registro OK, correo duplicado, normalización de correo, error de red | Auth remoto + caché |
| `TramiteRepositoryTest` | Crear OK, crear sin red, aprobar OK, aprobar sin legibilidad, devolver con observación, devolver sin observación, reenviar, eliminar, refrescar caché | CRUD y reglas |
| `LoginViewModelTest` | Login OK navega, error muestra mensaje, tipo incorrecto, campos vacíos | Estado de login |
| `MisTramitesViewModelTest` | Filtros, contadores, alerta de devuelto, avisos sin leer, refresco | Estado de pantalla |
| `BandejaNotarioViewModelTest` | Filtros, contadores, aprobar, devolver, trámite finalizado bloqueado | Reglas del notario |
| `CrearTramiteViewModelTest` | Validación de nombre/RUN/correo/comuna, doble envío, firma obligatoria, éxito, error de backend | Formulario y guardado |
| `IndicadoresRepositoryTest` | UF OK, respuesta sin UF, excepción de red → `null`/`Result.failure` | Aislamiento de API externa |

### 10.4 Ejemplo de test con StateFlow

`MisTramitesViewModel.uiState` usa `SharingStarted.WhileSubscribed`, por lo que el estado no emite si nadie lo colecciona. En el test:

```kotlin
@Test
fun `filtra observados`() = runTest {
    val vm = MisTramitesViewModel(fakeRepo, "USR-001")

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        vm.uiState.collect {}
    }

    vm.onFiltro(FiltroTramites.OBSERVADOS)
    advanceUntilIdle()

    assertEquals(1, vm.uiState.value.tramites.size)
}
```

Para pruebas de ViewModels con `viewModelScope`, usar `Dispatchers.setMain(StandardTestDispatcher(testScheduler))` y `advanceUntilIdle()`.

### 10.5 Cobertura

**Opción recomendada para la entrega:** Android Studio → clic derecho en `app/src/test` → **Run 'Tests in notix' with Coverage**. Revisar el reporte de clases en:

- `viewmodel`
- `repository`
- `data/validation`
- `data/remote` (mappers y manejo de errores)

**Evidencia:** captura del reporte con el porcentaje ≥ 80 % en `docs/evidencias/cobertura.png`.

**Opción automatizada:** si el equipo quiere un umbral en CI, evaluar Kover. No es obligatorio y puede consumir tiempo de configuración con AGP 9. La pauta acepta la cobertura de Android Studio.

### 10.6 Comando de ejecución

```bash
cd Notix
./gradlew testDebugUnitTest
```

Reporte HTML:

```text
Notix/app/build/reports/tests/testDebugUnitTest/index.html
```

---

## 11. APK firmado

### 11.1 Crear el KeyStore

```bash
cd Notix
mkdir -p keystore
keytool -genkeypair -v \
  -keystore keystore/notix_release.jks \
  -alias notix \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Guardar contraseñas y alias. Para la entrega académica se pueden documentar en `keystore.properties`.

### 11.2 `keystore.properties`

Crear en `Notix/keystore.properties`:

```properties
storeFile=keystore/notix_release.jks
storePassword=TU_CLAVE
keyAlias=notix
keyPassword=TU_CLAVE
```

### 11.3 Configurar `Notix/app/build.gradle.kts`

```kotlin
import java.util.Properties

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

android {
    // ...
    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }
}
```

> Mantener el bloque `optimization` que ya existe en el proyecto. Si AGP 9 rechaza alguna propiedad, ejecutar `./gradlew :app:assembleRelease` y ajustar solo lo que marque el error.

### 11.4 Generar y verificar

```bash
cd Notix
./gradlew :app:assembleRelease
```

El APK queda en:

```text
Notix/app/build/outputs/apk/release/app-release.apk
```

Verificar firma:

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

Si `apksigner` no está en el `PATH`, usar la versión de Build Tools del SDK:

```bash
$ANDROID_HOME/build-tools/<version>/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
```

### 11.5 Incluir en el repositorio

La pauta pide el `.jks` y el APK. Para esta entrega académica:

- Incluir `Notix/keystore/notix_release.jks`.
- Incluir `Notix/keystore.properties` (o documentar las credenciales en el README si se prefiere no versionarlo).
- Incluir el APK en `Notix/app/build/outputs/apk/release/app-release.apk` o en una carpeta `entregables/`.
- Si `.gitignore` bloquea `*.jks`, agregar una excepción:

```gitignore
!Notix/keystore/notix_release.jks
```

> Advertencia: en un proyecto real nunca se versiona una llave privada ni contraseñas. Aquí se hace solo porque la pauta lo exige y la llave es de demostración.

### 11.6 Evidencia

Capturas para `docs/evidencias/`:

- `apk-firmado.png`: APK generado y `apksigner` OK.
- `keystore.png`: archivo `.jks` y configuración.
- `cobertura.png`: reporte ≥ 80 %.
- `trello.png`: tablero con tareas.
- `demo-*.png`: flujos principales.

---

## 12. README, Trello, GitHub y AVA

### 12.1 README raíz — 6 apartados obligatorios

El `README.md` actual no cumple la estructura EP4. Reemplazarlo o ampliarlo con:

1. **Nombre del proyecto:** `Notix — Caso NotariaX`.
2. **Integrantes:** nombre y apellido de ambos.
3. **Funcionalidades:** login/registro, catálogo, creación, firma simulada, bandeja, aprobar/devolver, corregir/reenviar, avisos, API externa, cancelar.
4. **Endpoints utilizados:**
   - API externa: `GET https://mindicador.cl/api`.
   - `ms-usuarios`: tabla de endpoints.
   - `ms-tramites`: tabla de endpoints.
5. **Pasos para ejecutar:**
   - Backend: dos terminales, puertos 8081/8082, H2.
   - App: `./gradlew installDebug`, emulador `10.0.2.2`, teléfono físico IP LAN.
   - Credenciales demo.
6. **Captura del APK firmado y `.jks`:** imágenes y ruta del archivo.

Agregar además:

- Diagrama de arquitectura.
- Tecnologías y versiones.
- Advertencia de datos ficticios.
- Limitaciones (HTTP sin cifrar, contraseñas de demo, firma simulada).
- Enlace al tablero Trello.

### 12.2 Trello

Tablero con columnas:

- Backlog
- Por hacer
- En progreso
- En revisión
- Terminado

Tarjetas mínimas, asignadas y con fecha:

| Tarjeta | Responsable sugerido |
|---|---|
| Levantar `ms-usuarios` + H2 + seeder | Integrante A |
| Levantar `ms-tramites` + CRUD + estados | Integrante B |
| Retrofit + DTOs + sesión | Integrante A |
| Repositorios remotos + caché Room | Integrante B |
| Firma simulada (Canvas/e-token/notariada) | Integrante B |
| Corregir/reenviar + cancelar trámite | Integrante A |
| Legibilidad obligatoria + bloqueo de estados | Integrante B |
| Logout Persona/Empresa + botón atrás | Integrante A |
| API externa Mindicador + tarjeta UI | Integrante A |
| Pruebas unitarias + cobertura ≥ 80 % | Ambos |
| APK firmado + verificación | Integrante B |
| README + Trello + evidencias | Ambos |
| Ensayo de demo y congelamiento | Ambos |

Adjuntar captura del tablero en `docs/evidencias/trello.png`.

### 12.3 GitHub

- Repositorio público con acceso al docente.
- Ramas sugeridas:
  - `main`
  - `feature/backend-usuarios`
  - `feature/backend-tramites`
  - `feature/android-retrofit`
  - `feature/ui-flujos-ep4`
  - `feature/tests`
  - `feature/signing`
- Commits técnicos, no “update” genéricos. Ejemplos:

```text
feat(ms-usuarios): agrega entidad, endpoints y seeder H2
feat(ms-tramites): implementa CRUD y máquina de estados
feat(app): integra Retrofit con ms-usuarios y ms-tramites
feat(app): agrega firma simulada y reenvío tras devolución
test(app): cubre AuthRepository y TramiteRepository con MockK
build(app): configura signingConfigs y genera APK release
docs: agrega README EP4 y evidencias
```

- **Ambos integrantes deben tener commits en la app y en el backend.** La pauta lo pide explícitamente.
- No hacer commits después de la fecha límite.

### 12.4 AVA

- Enlace grupal: subir código de app + microservicios + APK + `.jks` + evidencias + Trello.
- Enlace individual: activarlo ambos con nombre, apellido, número de equipo y nombre de la app.
- Congelar el repositorio antes de la fecha y hora límite.

---

## 13. Cronograma sugerido (10 días hábiles)

| Día | Objetivo | Entregable |
|---|---|---|
| 1 | Congelar baseline, crear estructura `backend/`, agregar dependencias Retrofit/MockK | Proyecto compila con nuevas dependencias |
| 2 | `ms-usuarios` con entidad, endpoints, seeder y H2 | Login/registro funcionando por Postman |
| 3 | `ms-tramites` con entidades, CRUD, estados, avisos e historial | CRUD completo por Postman |
| 4 | Retrofit + DTOs + `SessionManager` + repositorios remotos | App lista para consumir backend |
| 5 | Integración de login, trámites, avisos y refresco | Flujo principal contra backend |
| 6 | Firma simulada, legibilidad, corregir/reenviar, cancelar, logout | Fase 1 cerrada |
| 7 | API externa Mindicador + tarjeta UI + fallback | API visible sin bloquear |
| 8 | Pruebas unitarias de repositorios y ViewModels | Suites verdes |
| 9 | Cobertura ≥ 80 %, APK firmado, README y Trello | Evidencias EP4 |
| 10 | Ensayo de demo, corrección de bugs, congelamiento | Entrega AVA |

> Si el tiempo se reduce, priorizar en este orden: backend funcional → integración CRUD → flujos de UI → API externa → pruebas → firma APK → README/Trello. Sin embargo, todos los ítems suman; no dejar pruebas ni APK para el final.

---

## 14. Checklist de aceptación y guion de demo

### 14.1 Checklist técnico

- [ ] `./gradlew assembleDebug` compila sin errores.
- [ ] `ms-usuarios` arranca en 8081 y responde login/registro.
- [ ] `ms-tramites` arranca en 8082 y responde CRUD.
- [ ] La app inicia sesión contra el backend.
- [ ] La app crea un trámite y el backend lo persiste.
- [ ] El notario ve el trámite y lo aprueba con legibilidad marcada.
- [ ] El notario no puede aprobar sin checkbox.
- [ ] El notario no puede actuar sobre un trámite finalizado.
- [ ] La persona ve el cambio de estado y el aviso.
- [ ] La persona corrige y reenvía un trámite devuelto.
- [ ] La persona puede cancelar un trámite no finalizado.
- [ ] La firma simulada se exige antes de enviar.
- [ ] La tarjeta de UF se muestra y tiene fallback si falla la API.
- [ ] Logout funciona para Persona, Empresa y Notario.
- [ ] Las pruebas unitarias pasan.
- [ ] La cobertura de lógica es ≥ 80 %.
- [ ] El APK release está firmado y verificado.
- [ ] El README tiene los 6 apartados.
- [ ] Trello tiene tareas distribuidas y evidencia.
- [ ] Ambos integrantes tienen commits en app y backend.
- [ ] Repositorio congelado antes del plazo.

### 14.2 Guion de demo (5–7 minutos)

1. Mostrar los dos microservicios corriendo y la consola H2.
2. Abrir la app y mostrar la tarjeta de UF.
3. Login como Persona (`ana.demo@notariax.test` / `demo123`).
4. Crear un trámite:
   - Elegir formato.
   - Completar antecedentes.
   - Adjuntar documento.
   - Dibujar firma simple.
   - Confirmar envío.
5. Mostrar el trámite en “Mis trámites”.
6. Cerrar sesión y entrar como Notario (`notario.demo@notariax.test` / `notario123`).
7. Abrir el trámite:
   - Intentar aprobar sin checkbox → mostrar que está bloqueado.
   - Marcar legibilidad y aprobar.
8. Volver a Persona y mostrar estado `FINALIZADO` + aviso.
9. Crear/devolver otro trámite con observación.
10. Como Persona, corregir y reenviar.
11. Como Notario, verlo nuevamente en `ENVIADO`.
12. Cancelar un trámite no finalizado (CRUD DELETE).
13. Mostrar pruebas, cobertura y APK firmado.
14. Mostrar README y Trello.

---

## 15. Riesgos y mitigaciones

| Riesgo | Probabilidad | Mitigación |
|---|---|---|
| El emulador no conecta a `localhost` | Alta | Usar `10.0.2.2`; documentar IP LAN para teléfono físico. |
| Falta permiso de Internet o cleartext | Alta | Agregar `INTERNET` y `usesCleartextTraffic="true"`. |
| El backend no está corriendo en la defensa | Media | Script de arranque, README claro y datos demo. Probar en frío. |
| Room borra datos por cambio de esquema | Media | Es caché; el backend es la fuente de verdad. Documentar. |
| MockK falla con Kotlin 2.2 | Media | Subir MockK a 1.14.x si hay error de metadatos. |
| AGP 9 rechaza `signingConfigs` o `optimization` | Media | Probar `assembleRelease` temprano; ajustar DSL según error. |
| Cobertura < 80 % | Media | Medir desde el día 8; priorizar repositorios y ViewModels. |
| UI depende de Room y no se refresca | Media | `refresh()` al entrar y después de cada acción; polling opcional. |
| Conflictos de merge | Media | Ramas por funcionalidad, PRs pequeños, `main` siempre compilando. |
| Commits solo de un integrante | Alta | Asignar tareas reales a ambos desde Trello y verificar `git shortlog -sne`. |
| Commit después del plazo | Crítica | Congelar el repo y no tocar `main` después de la hora límite. |

---

## 16. Mapa de archivos a crear/modificar

### 16.1 Nuevos

| Archivo | Propósito |
|---|---|
| `backend/ms-usuarios/**` | Microservicio de usuarios. |
| `backend/ms-tramites/**` | Microservicio de trámites, documentos, historial y avisos. |
| `Notix/app/src/main/java/com/duoc/notix/data/remote/**` | DTOs, APIs, clientes Retrofit y mappers. |
| `Notix/app/src/main/java/com/duoc/notix/data/session/SessionManager.kt` | Sesión persistida. |
| `Notix/app/src/main/java/com/duoc/notix/repository/IndicadoresRepository.kt` | API externa aislada. |
| `Notix/app/src/main/java/com/duoc/notix/viewmodel/IndicadoresViewModel.kt` | Estado de la tarjeta UF. |
| `Notix/app/src/main/java/com/duoc/notix/ui/components/FirmaSimuladaDialog.kt` | Firma Canvas/e-token/notariada. |
| `Notix/app/src/main/java/com/duoc/notix/ui/components/CorreccionTramiteDialog.kt` | Corregir y reenviar. |
| `Notix/app/src/test/java/com/duoc/notix/**` | Suites unitarias. |
| `Notix/keystore/notix_release.jks` | Llave de firma. |
| `Notix/keystore.properties` | Credenciales de firma (académico). |
| `docs/evidencias/**` | Capturas de APK, cobertura, Trello y demo. |

### 16.2 Modificados

| Archivo | Cambio |
|---|---|
| `Notix/gradle/libs.versions.toml` | Retrofit, OkHttp, MockK, coroutines-test. |
| `Notix/app/build.gradle.kts` | Dependencias, `buildConfig`, `testOptions`, `signingConfigs`. |
| `Notix/app/src/main/AndroidManifest.xml` | `INTERNET`, cleartext. |
| `Notix/app/src/main/java/com/duoc/notix/MainActivity.kt` | Sesión, logout, back, URLs, navegación. |
| `repository/AuthRepository.kt` | Login/registro remoto, mapeo de errores. |
| `repository/TramiteRepository.kt` | CRUD remoto + caché Room + refresco. |
| `viewmodel/LoginViewModel.kt` | Inyección de repositorio, estado de error. |
| `viewmodel/AuthViewmodel.kt` | Inyección de repositorio. |
| `viewmodel/TramitesViewmodel.kt` | Refresco, reenvío, eliminación, firma. |
| `viewmodel/BandejaNotarioViewModel.kt` | Refresco, legibilidad, bloqueo de estados. |
| `viewmodel/CrearTramiteViewModel.kt` | Firma obligatoria, creación remota, ID del backend. |
| `ui/screens/CrearTramiteScreen.kt` | Paso de firma, legibilidad inicial `false`. |
| `ui/screens/MisTramitesScreen.kt` | Corregir/reenviar, cancelar, logout, detalle actualizado. |
| `ui/screens/BandejaNotarioScreen.kt` | Checkbox, bloqueo de estados, refresco. |
| `ui/screens/CatalogoFormatosScreen.kt` | Tarjeta de UF/UTM. |
| `ui/screens/AvisosScreen.kt` | Logout, marcar leído remoto, navegación. |
| `model/Tramite.kt` | `firmaEvidencia`, `fechaCreacion`, `fechaActualizacion`. |
| `data/local/Entities.kt` | Campos nuevos. |
| `data/local/NotixDatabase.kt` | Versión 3. |
| `data/local/Mappers.kt` | Mapeo de campos nuevos. |
| `data/local/DatabaseSeeder.kt` | Alinear con backend o dejar como respaldo. |
| `README.md` | 6 apartados EP4, endpoints, ejecución, evidencias. |
| `docs/Documentacion_Notix.md` | Actualizar estado final. |

---

## 17. Orden exacto de implementación recomendado

1. **Backend primero, sin tocar la app.**
   - Crear `ms-usuarios`, probar login/registro con `curl` o Postman.
   - Crear `ms-tramites`, probar CRUD y estados.
2. **Configuración Android.**
   - Dependencias, `buildConfig`, manifest, `SessionManager`.
3. **Capa remota.**
   - DTOs, APIs, clientes, mappers.
4. **Repositorios.**
   - `AuthRepository` remoto.
   - `TramiteRepository` remoto + caché.
5. **ViewModels.**
   - Inyección, refresco, errores.
6. **UI Fase 1.**
   - Firma, reenvío, legibilidad, logout, cancelar.
7. **API externa.**
   - Mindicador + tarjeta.
8. **Pruebas.**
   - Refactor de testabilidad, suites, cobertura.
9. **APK.**
   - KeyStore, `signingConfigs`, `assembleRelease`, verificación.
10. **Documentación y entrega.**
    - README, Trello, evidencias, AVA.

> Regla práctica: después de cada paso, ejecutar `./gradlew :app:compileDebugKotlin` y arrancar ambos microservicios. No acumular cambios sin compilar.

---

## 18. Resumen de una página

- **Lo que ya hay:** app Compose funcional, Room, login local, creación de trámites, bandeja del notario, avisos.
- **Lo que falta:** backend Spring Boot, Retrofit, API externa, firma simulada, reenvío, legibilidad obligatoria, logout Persona/Empresa, DELETE, pruebas, APK firmado, README/Trello.
- **Arquitectura:** app → ViewModels → repositorios → Retrofit (8081/8082) + Room caché + API externa aislada.
- **Backend:** dos microservicios con H2 en archivo, seeders y CRUD; el backend genera historial y avisos.
- **API externa:** Mindicador `GET /api`, tarjeta de UF en catálogo, fallback si falla.
- **Pruebas:** JUnit + MockK + coroutines-test; cobertura ≥ 80 % en ViewModels/Repositorios/validación.
- **Entrega:** `.jks` + `assembleRelease` + `apksigner`, README de 6 puntos, Trello, commits de ambos, AVA grupal e individual.
- **Riesgo principal:** que la app no compile o el backend no arranque en la defensa. Probar el flujo completo al menos dos veces antes de congelar.
