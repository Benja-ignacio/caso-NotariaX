# Notix — Documentación técnica y estado del proyecto

> Proyecto académico DSY1105 · Caso NotariaX · Duoc UC
> Última actualización: 23/11/2026

---

## 1. Resumen

**Notix** es el MVP Android del caso **NotariaX**: una app para gestionar y seguir trámites notariales digitales con datos ficticios.

El flujo implementado cubre:

1. Registro e inicio de sesión con tres perfiles: **Persona**, **Empresa** y **Notario**.
2. Catálogo ficticio de formatos de trámite.
3. Creación de un trámite con antecedentes, documento adjunto, firma simulada y confirmación.
4. Envío simulado al notario.
5. Bandeja del notario con filtros y contadores.
6. Revisión del trámite: **aprobar/finalizar** (exigiendo legibilidad) o **devolver con observaciones**.
7. Actualización automática de estado, historial y avisos.
8. Consulta del estado, historial y observaciones por parte del solicitante.
9. Corrección y reenvío cuando el trámite fue devuelto.
10. Cierre de sesión para Persona, Empresa y Notario.
11. Sincronización de usuarios, trámites y avisos contra los microservicios con Retrofit.
12. Feriados externos de Nager.Date con fallback si la API no responde.

La app **no debe usarse para trámites notariales reales**. No integra servicios productivos, firmas reales, biometría real ni bases de datos de NotariaX.

---

## 2. Estado general

| Área | Estado |
|---|---|
| Proyecto Android + Jetpack Compose | ✅ Funcional |
| Persistencia local con Room | ✅ Implementada |
| Microservicios Spring Boot (`backend/`) | ✅ Implementados y probados |
| Integración app Android ↔ backend | ✅ Retrofit para login, registro, trámites y avisos |
| API externa en la app | ✅ Nager.Date integrada; fallback si no responde |
| Autenticación local (login/registro) | ✅ Implementada |
| Flujo Persona/Empresa | ✅ Implementado en su mayoría |
| Flujo Notario | ✅ Implementado en su mayoría |
| Historial y avisos en base de datos | ✅ Implementado |
| Selector nativo de archivos | ✅ Integrado (solo metadatos) |
| Notificaciones del sistema | ✅ Locales al sincronizar; pendiente push con app cerrada |
| Firma simulada (trazo/mock) | ✅ Implementada y obligatoria antes de enviar |
| Corrección y reenvío tras devolución | ✅ Implementada |
| Eliminar trámites no finalizados | ✅ Implementado con confirmación y backend |
| Confirmación de legibilidad por el notario | ✅ Obligatoria para aprobar |
| Pruebas unitarias/UI | ✅ 82 pruebas JVM + 7 pruebas DAO; cobertura de línea 96,30 %; faltan pruebas de UI |
| Navegación completa (perfil, atrás, logout) | ⚠️ Perfil y logout implementados; falta manejo del botón atrás |
| Documentación | ✅ Este documento + README actualizado |

> El proyecto **compila** con el SDK configurado. Ver sección 12 para la verificación y requisitos de entorno.

---

## 3. Estructura del repositorio

```text
caso-NotariaX/
├── README.md                      # Entrada rápida: cómo ejecutar y estado
├── backend/                       # Microservicios Spring Boot (Fase 2 EP4)
│   ├── pom.xml                    # Proyecto Maven padre
│   ├── ms-usuarios/               # Puerto 8081
│   └── ms-tramites/               # Puerto 8082
├── docs/
│   ├── contexto.md                # Enunciado y alcance del caso
│   ├── implementacion/                # Planes, pauta y estado de continuidad EP4
│   └── Documentacion_Notix.md     # Este documento (estado real)
│
├── index.html                     # Prototipo web legado (no es la app)
├── css/styles.css                 # Estilos del prototipo web legado
├── views/                         # Pantallas HTML del prototipo legado
│
└── Notix/                         # Proyecto Android (entrega principal)
    ├── app/
    │   ├── build.gradle.kts
    │   └── src/main/
    │       ├── AndroidManifest.xml
    │       ├── java/com/duoc/notix/
    │       │   ├── MainActivity.kt
    │       │   ├── model/                 # Modelos de dominio
    │       │   ├── data/
    │       │   │   ├── local/             # Room: entidades, DAO, DB, mappers, seeder
    │       │   │   ├── remote/            # Retrofit, DTOs, APIs y mapeos
    │       │   │   └── validation/        # Validación de RUN
    │       │   ├── repository/            # AuthRepository + TramiteRepository
    │       │   ├── viewmodel/             # Un ViewModel por flujo
    │       │   ├── ui/
    │       │   │   ├── screens/           # Pantallas Compose
    │       │   │   ├── components/        # Componentes reutilizables
    │       │   │   └── theme/             # Colores, tipografía, tema
    │       │   └── util/                  # Formateo de RUT, documento y fechas
    │       └── res/                       # Recursos Android
    ├── gradle/libs.versions.toml
    └── gradlew / gradlew.bat
```

### Sobre el prototipo web

`index.html`, `css/` y `views/` son el **prototipo navegable original** en HTML. No forman parte de la app Android ni comparten código con ella. Se conservan como referencia visual y de flujo. No tienen persistencia: son formularios estáticos que navegan por URL.

---

## 4. Tecnologías

| Elemento | Versión / decisión |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | UI → ViewModel → Repository → Room |
| Persistencia | Room 2.8.5 con KSP |
| SDK | compileSdk 37, targetSdk 36, minSdk 24 |
| Build | Android Gradle Plugin 9.2.1 |
| Java | 11 |
| Navegación | Estado en `MainActivity` (`enum Pantalla`), sin Navigation Compose |
| Datos | 100 % ficticios, definidos en el seeder |
| Backend | Java 21, Spring Boot 4.2, Spring Web MVC, Spring Data JPA, Maven Wrapper |
| Persistencia backend | H2 2.5 en archivo (`data/`) con Hibernate 7 |
| Puertos backend | `ms-usuarios` 8081, `ms-tramites` 8082 |
| Integración app ↔ backend | Retrofit 2.11 + OkHttp 4.12 + Gson, contra `10.0.2.2:8081/8082` en emulador |
| API externa | Nager.Date (`https://date.nager.at/api/v3/NextPublicHolidays/CL`) para el próximo feriado |

Dependencias relevantes declaradas en `Notix/gradle/libs.versions.toml`:

- `androidx.room:room-runtime`, `room-ktx`, `room-compiler` (KSP)
- `androidx.lifecycle:lifecycle-viewmodel-compose`
- `androidx.compose.material3` y `material-icons-extended`
- `androidx.activity:activity-compose`
- `com.squareup.retrofit2:retrofit` y `converter-gson`
- `com.squareup.okhttp3:logging-interceptor`
- `io.mockk:mockk` y `org.jetbrains.kotlinx:kotlinx-coroutines-test` para pruebas
- JaCoCo para cobertura de línea

---

## 5. Arquitectura

```text
┌──────────────────────────────┐
│  Compose Screens             │  Solo muestran estado y envían eventos
│  ui/screens/*                │
└──────────────┬───────────────┘
               │ eventos / StateFlow / mutableStateOf
┌──────────────▼───────────────┐
│  ViewModels                  │  Estado de pantalla, validaciones de UI,
│  viewmodel/*                 │  llamadas al repositorio
└──────────────┬───────────────┘
               │ suspend / Flow
┌──────────────▼───────────────┐
│  Repositories                │  Operaciones de negocio y coordinación
│  repository/*                │  (crear trámite, cambiar estado, avisos)
└──────────────┬───────────────┘
               │ DAOs / entidades
┌──────────────▼───────────────┐
│  Room                        │  Persistencia local en SQLite
│  data/local/*                │
└──────────────────────────────┘
```

Decisiones actuales:

- La UI **no consulta Room directamente**; excepto la construcción de dependencias en los `factory`.
- `AuthRepository` y `TramiteRepository` encapsulan las operaciones.
- La validación de RUN vive en `data/validation/RutValidator`.
- Algunas validaciones de formulario están en el ViewModel y otras en la pantalla de registro (deuda técnica menor).
- La sesión se maneja con estado en memoria/`rememberSaveable` en `MainActivity`; **no hay sesión persistida**.

### 5.1 Backend EP4 y sincronización

`backend/` es un proyecto Maven independiente con dos microservicios Spring Boot. La app se conecta con Retrofit:

```text
App Android
├── Compose → ViewModels → Repositories
│                              ├── Room (caché local y lectura offline)
│                              ├── Retrofit ms-usuarios :8081
│                              ├── Retrofit ms-tramites :8082
│                              └── Retrofit Nager.Date (API externa)
└── data/remote/ (clientes, DTOs y mapeos)
```

- `ms-usuarios` expone registro, login, consulta, actualización y eliminación de usuarios. Normaliza el correo, valida RUN/documento y exige campos según Persona/Empresa/Notario.
- `ms-tramites` expone listado general y por solicitante, detalle, creación, aprobación/devolución, reenvío, eliminación y avisos.
- La máquina de estados es la misma de la app: `ENVIADO → FINALIZADO` exige legibilidad; `ENVIADO → DEVUELTO` exige observación; `DEVUELTO → ENVIADO` usa reenvío; `FINALIZADO` rechaza cambios y eliminación con `409`.
- Los seeders crean los mismos usuarios demo y cuatro trámites ficticios.
- La app escribe primero en el backend y luego actualiza Room con la respuesta. Al abrir Mis Trámites, Bandeja o Avisos, refresca la caché desde la API.
- Si el backend no responde, la UI conserva la última copia de Room; las escrituras muestran un error. El login admite fallback local si ya existe el usuario en Room.
- La tarjeta de feriados de `CatalogoFormatosScreen` consulta el próximo feriado en Nager.Date; cualquier fallo devuelve `null` y muestra un texto de respaldo.
- Contratos, ejemplos `curl` y ejecución: [`backend/README.md`](../backend/README.md).

---

## 6. Persistencia de datos (Room)

Esta es la parte central para la rúbrica: **los trámites, usuarios, documentos, historial y avisos sobreviven al cierre de la app**.

### 6.1 Base de datos

| Dato | Valor |
|---|---|
| Clase | `NotixDatabase` (`data/local/NotixDatabase.kt`) |
| Nombre del archivo | `notix_database` (SQLite dentro del sandbox de la app) |
| Versión | `3` |
| Exportación de esquema | `exportSchema = false` |
| Migraciones | `MIGRATION_2_3` explícita + `fallbackToDestructiveMigration()` |
| Instancia | Singleton con `getDatabase(context)` |
| Convertidores | `NotixTypeConverters` |

> **Migración 2 → 3:** agrega la columna `tramites.firmaEvidencia` con `ALTER TABLE ... ADD COLUMN`, por lo que conserva los datos locales. El fallback destructivo queda solo como red de seguridad para versiones futuras sin migración.

### 6.2 Tablas

#### `usuarios`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT (PK) | `USR-...` |
| `tipoCuenta` | TEXT | `PERSONA`, `EMPRESA`, `NOTARIO` |
| `nombreCompleto` | TEXT | En empresa, representante legal |
| `correo` | TEXT | Índice único |
| `contrasena` | TEXT | **Texto plano** (solo demo) |
| `run` | TEXT | Limpio, sin puntos ni guion |
| `numeroDocumento` | TEXT | Limpio |
| `telefono` | TEXT | Limpio |
| `comuna` | TEXT | Residencia / domicilio / comuna de notaría |
| `razonSocialEmpresa` | TEXT? | Solo empresa |
| `rutEmpresa` | TEXT? | Solo empresa |
| `giroEmpresa` | TEXT? | Solo empresa |
| `numeroNotaria` | INTEGER? | Solo notario |

#### `tramites`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT (PK) | `TX-2026-XXXX` |
| `formatoId`, `formatoNombre`, `formatoCategoria` | TEXT | Datos denormalizados del catálogo |
| `solicitanteId` | TEXT | Referencia lógica a `usuarios.id` (sin FK) |
| `solicitanteNombre` | TEXT | Copia del nombre al crear |
| `actuaEnNombreDe` | TEXT? | Razón social si aplica |
| `tipoFirma` | TEXT | `SIMPLE`, `AVANZADA`, `NOTARIADA` |
| `estado` | TEXT | `ENVIADO`, `DEVUELTO`, `FINALIZADO` |
| `antecedentes*` | TEXT | Nombre, documento, correo y comuna |
| `observacion` | TEXT? | Motivo de devolución |
| `legibilidadConfirmada` | INTEGER | Booleano; lo marca el notario al aprobar |
| `firmaEvidencia` | TEXT? | Evidencia ficticia: `SIMPLE_CANVAS`, `AVANZADA_TOKEN_SIMULADO`, `NOTARIADA_SIMULADA` |

#### `documentos`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | INTEGER (PK autoincremental) | |
| `tramiteId` | TEXT (FK → `tramites.id`) | `ON DELETE CASCADE` |
| `nombre` | TEXT | Solo nombre del archivo |
| `tamanoKb` | INTEGER | Tamaño simulado o estimado |
| `estado` | TEXT | `ACEPTADO`, `OBSERVADO` |

> **No se guarda el archivo ni su contenido.** El selector nativo entrega una URI, pero solo se persiste el nombre. No se copia el archivo al almacenamiento de la app ni se solicita permiso persistente de lectura. Ver sección 10.

#### `historial_eventos`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | INTEGER (PK autoincremental) | |
| `tramiteId` | TEXT (FK → `tramites.id`) | `ON DELETE CASCADE` |
| `descripcion` | TEXT | Texto del evento |
| `fechaHora` | TEXT | `dd/MM/yyyy HH:mm` (fecha real del dispositivo) |
| `rol` | TEXT | Quién realiza el cambio |

#### `avisos`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT (PK) | `AVS-...` |
| `destinatarioId` | TEXT | Usuario que recibe el aviso |
| `tramiteId` | TEXT | Trámite relacionado |
| `mensaje` | TEXT | Texto mostrado |
| `fechaHora` | TEXT | Fecha y hora |
| `leido` | INTEGER | Booleano |

> `avisos` no tiene FK ni índice; se consulta por `destinatarioId` y se ordena por `fechaHora` como texto.

### 6.3 DAOs

`TramiteDao`

- `insertTramite`, `insertDocumentos`, `insertHistorial`
- `insertTramiteCompleto(...)`: transacción que inserta trámite + documentos + historial
- `getTramiteConDetalles(id)`: `Flow<TramiteConDetalles?>`
- `getTramitesPorSolicitante(id)`: `Flow<List<TramiteConDetalles>>`
- `getTodosLosTramites()`: `Flow<List<TramiteConDetalles>>`
- `updateTramite(...)`
- `actualizarEstadoCompleto(...)`: transacción que actualiza estado + historial + aviso
- `reenviarTramiteCompleto(...)`: transacción que actualiza antecedentes/estado, reemplaza documentos y agrega historial + aviso

`UsuarioDao`

- `insertUsuario` (falla si hay duplicados)
- `upsertUsuario` (usado por el seeder)
- `getUsuarioPorCorreo(correo)`
- `getUsuarioPorId(id)`: `Flow`

`AvisoDao`

- `insertAviso`
- `getAvisosPorDestinatario(id)`: `Flow`
- `marcarAvisoLeido(id)`

Las consultas con `Flow` hacen que las pantallas se actualicen automáticamente cuando cambia la base de datos. Por eso, cuando el notario aprueba o devuelve un trámite, la persona ve el cambio sin reiniciar la app.

### 6.4 Mapeo dominio ↔ base de datos

- `Mappers.kt` convierte `Usuario`, `Tramite` y `Aviso` hacia/desde entidades.
- `TramiteConDetalles` usa `@Relation` para traer documentos e historial.
- El formato se guarda **denormalizado** dentro del trámite, de modo que un cambio futuro del catálogo no altere trámites históricos.

### 6.5 TypeConverters

Los enums se guardan como `String` (`name`) y al leerlos se usa `runCatching` con valor por defecto:

- `TipoCuenta` → `PERSONA`
- `EstadoTramite` → `ENVIADO`
- `TipoFirma` → `SIMPLE`
- `EstadoDocumento` → `ACEPTADO`

Esto evita crashes ante valores desconocidos, pero puede ocultar datos corruptos.

### 6.6 Datos iniciales (seeder)

`DatabaseSeeder.seedIfEmpty()` se ejecuta al iniciar `MainActivity`.

- Es **idempotente**: crea/actualiza los usuarios demo y solo inserta trámites si la tabla está vacía.
- Usuarios demo:

| Perfil | Correo | Contraseña |
|---|---|---|
| Persona | `ana.demo@notariax.test` | `demo123` |
| Notario | `notario.demo@notariax.test` | `notario123` |

- Trámites demo:

| ID | Formato | Estado |
|---|---|---|
| `TX-2026-0042` | Poder simple | Devuelto (con anexo observado) |
| `TX-2026-0039` | Declaración jurada simple | Enviado |
| `TX-2026-0031` | Autorización de viaje | Finalizado |
| `TX-2026-0001` | Poder simple | Enviado |

- Aviso demo: `AVS-001`.

> La contraseña de empresa no está sembrada; debe registrarse una cuenta desde la app.

### 6.7 Qué persiste y qué no

| Dato | ¿Persiste al cerrar la app? | Dónde |
|---|---|---|
| Copia remota de usuarios, trámites, documentos, historial y avisos | ✅ Sí | H2 de `ms-usuarios` / `ms-tramites` |
| Recuperación tras borrar la caché local | ✅ Sí, al volver a sincronizar | Backend |
| Usuarios registrados | ✅ Sí | Room (`usuarios`) |
| Trámites creados | ✅ Sí | Room (`tramites`) |
| Documentos (nombre, tamaño, estado) | ✅ Sí | Room (`documentos`) |
| Historial de estados | ✅ Sí | Room (`historial_eventos`) |
| Avisos y estado leído | ✅ Sí | Room (`avisos`) |
| Sesión iniciada / usuario actual | ❌ No | Estado de `MainActivity` |
| Contraseña | ⚠️ Sí, en texto plano | Room (`usuarios`) |
| Archivo adjunto real (contenido/URI) | ❌ No | Solo se guarda el nombre |
| Firma dibujada | ⚠️ Solo evidencia ficticia | Room (`tramites.firmaEvidencia`) |
| Notificaciones del sistema | ❌ No | Solo avisos internos en Room |
| Filtros y selección de pantalla | ❌ No | Estado Compose en memoria |

### 6.8 Backup de Android

`AndroidManifest.xml` declara:

- `android:allowBackup="true"`
- `android:fullBackupContent="@xml/backup_rules"`
- `android:dataExtractionRules="@xml/data_extraction_rules"`

Esto significa que Android **podría respaldar la base de datos** según las reglas del dispositivo. Para un MVP con datos ficticios no es crítico, pero conviene revisarlo si se quiere evitar cualquier respaldo.

### 6.9 Consecuencias prácticas

- Cerrar y abrir la app **mantiene** trámites, usuarios, avisos e historial.
- Cambiar la versión de la base de datos exige una migración explícita; la versión 2 → 3 conserva datos. El `fallbackToDestructiveMigration()` solo actúa si falta una ruta de migración.
- Desinstalar la app **borra** la base de datos.
- Borrar datos de la app desde Ajustes **borra** la base de datos.
- La app vuelve siempre a la pantalla de bienvenida porque la sesión no se guarda.
- El archivo elegido con el selector **no se conserva**; solo su nombre.

---

## 7. Funcionalidades implementadas

### 7.1 Acceso y cuentas

- `WelcomeScreen`: entrada a login y registro.
- `LoginScreen`: selector Persona / Empresa / Notario, correo, contraseña, mostrar/ocultar clave.
- Botón “Simular huella digital”: ejecuta el mismo login; **no usa biometría real**.
- `RegisterScreen`: formulario dinámico según tipo de cuenta.
  - Persona: nombre, RUT, N° documento, correo, teléfono, comuna.
  - Empresa: razón social, RUT empresa, giro, representante legal, RUN representante, documento, correo, teléfono, comuna.
  - Notario: nombre, RUN, documento, N° notaría, comuna, correo, teléfono.
  - Validaciones: RUN con dígito verificador, documento de 9 dígitos, correo, teléfono, número de notaría, contraseña mínima y confirmación.
- `AuthRepository`: evita correos duplicados, normaliza el correo y valida tipo de cuenta/clave.
- Al autenticarse, redirige a Mis Trámites (Persona/Empresa) o Bandeja del Notario.

### 7.2 Flujo Persona / Empresa

- `CatalogoFormatosScreen`: 3 formatos ficticios (`F01`, `F02`, `F03`) con firma requerida.
- `CrearTramiteScreen`:
  - Carga antecedentes desde el usuario.
  - Edición de nombre, RUN, correo y comuna con validaciones.
  - Campo “en representación de” para Empresa.
  - Selector nativo de archivos (`ActivityResultContracts.GetContent`).
  - Declaración de legibilidad/veracidad, desmarcada por defecto y obligatoria para continuar.
  - Paso de firma simulada según `TipoFirma`: trazo en `Canvas` (SIMPLE), código e-token de 6 dígitos (AVANZADA) o autorización notarial (NOTARIADA).
  - Modal de confirmación con resumen y evidencia de firma.
  - Guarda el trámite, la evidencia ficticia y crea un aviso.
- `MisTramitesScreen`:
  - Contadores: en revisión, observados, finalizados.
  - Filtros: todos, en curso, observados, finalizados.
  - Banner de alerta para trámite devuelto.
  - Detalle con antecedentes, documentos, firma simulada, observación del notario e historial.
  - Si el trámite está `DEVUELTO`, permite **Corregir y reenviar**: edita antecedentes, exige reemplazar documentos observados, limpia la observación, vuelve a `ENVIADO` y registra historial + aviso.
  - Si el trámite no está `FINALIZADO`, permite **Eliminar trámite** con confirmación. Llama a `DELETE /api/tramites/{id}` y borra de Room el trámite, documentos, historial y avisos.
  - Cierre de sesión con confirmación.
  - Al sincronizar avisos nuevos, solicita permiso de notificaciones en Android 13+ y lanza una notificación local por cada aviso no leído que no se haya notificado antes.
- `AvisosScreen`: lista de avisos, marca como leído al tocar y permite cerrar sesión.

### 7.3 Flujo Notario

- `BandejaNotarioScreen`:
  - Muestra todos los trámites (sin asignación por notaría).
  - Contadores y filtros.
  - Tarjetas con solicitante, comuna, correo, observación y estado.
- Diálogo de revisión:
  - Muestra antecedentes, documentos y firma simulada.
  - Solo permite actuar sobre trámites `ENVIADO`; un `FINALIZADO` o `DEVUELTO` se muestra en modo lectura.
  - **Aprobar** exige marcar el checkbox de legibilidad; guarda `legibilidadConfirmada = true`, estado `FINALIZADO`, historial y aviso.
  - **Devolver** exige observación obligatoria; estado `DEVUELTO`, historial y aviso.
  - Las transiciones inválidas también se rechazan en el repositorio, no solo en la UI.

### 7.4 UI y recursos

- Tema propio (`NotixColors`, `Theme`, `Type`).
- Componentes: `TramiteCard`, `EstadoChip`, `BarraInferiorPersona`, `NotixTextField`, transformaciones visuales de RUT/documento.
- `PerfilScreen`: datos de la cuenta, empresa/notaría y cierre de sesión.
- `BarraInferiorPersona`: tres tabs reales (`Trámites`, `Avisos`, `Perfil`). Se eliminó el “Inicio” vacío.
- Animaciones: contadores, chips, visibilidad de errores, transiciones básicas.
- Formato de fechas y documentos.
- Icono/logo de la app.

### 7.5 Backend Spring Boot (Fase 2)

- `ms-usuarios`:
  - `POST /api/usuarios/registro`, `POST /api/usuarios/login`, `GET /api/usuarios/{id}`.
  - `GET`, `PUT` y `DELETE` adicionales para completar el CRUD.
  - Respuesta sin contraseña; errores `400/401/403/404/409` con mensaje.
- `ms-tramites`:
  - `GET /api/tramites`, `GET /api/tramites/solicitante/{id}`, `GET /api/tramites/{id}`.
  - `POST /api/tramites`, `PUT /api/tramites/{id}/estado`, `PUT /api/tramites/{id}/reenvio`, `DELETE /api/tramites/{id}`.
  - `GET /api/avisos/{usuarioId}` y `PUT /api/avisos/{id}/leido`.
  - Historial y avisos generados por el backend, no por el cliente.
- Ambas bases H2 se crean como archivo y se reinician borrando `backend/ms-*/data/`.
- Verificación: 11 pruebas (`./mvnw test`) y llamadas HTTP reales a los endpoints principales.

### 7.6 Integración Retrofit y API externa

- `data/remote/ApiConfig.kt` define las URLs:
  - Emulador: `http://10.0.2.2:8081/` y `http://10.0.2.2:8082/`.
  - Dispositivo físico: `http://127.0.0.1:8081/` y `http://127.0.0.1:8082/`, junto con `adb reverse tcp:8081 tcp:8081` y `adb reverse tcp:8082 tcp:8082`.
  - `https://date.nager.at/` para feriados.
- Clientes Retrofit separados con OkHttp, Gson y logging básico.
- `AuthRepository` usa `ms-usuarios` para login/registro y guarda el usuario en Room. Si no hay red, el login puede validar contra la copia local.
- `TramiteRepository` envía creación, aprobación, devolución, reenvío y lectura de avisos al backend; después guarda la respuesta en Room.
- Al entrar a Mis Trámites, Bandeja del Notario o Avisos se ejecuta una sincronización `GET` y se reemplazan documentos/historial locales del trámite.
- `FeriadosRepository` consulta Nager.Date y **nunca lanza error**: `runCatching` devuelve `null` y la tarjeta del catálogo muestra “Feriados no disponibles”.
- Los errores HTTP se leen desde `message` del backend; los errores de red se muestran como falta de conexión.
- El manifiesto declara `INTERNET` y `usesCleartextTraffic="true"` para el backend HTTP local. Es una decisión académica; en producción se usaría HTTPS.

### 7.7 Pruebas unitarias y cobertura

- **82 pruebas JVM** con JUnit, MockK y `kotlinx-coroutines-test`.
- **7 pruebas DAO instrumentadas** con Room en memoria (`NotixDaoTest`).
- Cubren:
  - `RutValidator`, fechas y formateadores.
  - `AuthRepository` y `TramiteRepository` con APIs/DAOs simulados.
  - Mapeos DTO ↔ dominio y errores HTTP/red.
  - ViewModels de login, registro, trámites, bandeja, creación, avisos, perfil y feriados.
- **Cobertura de línea: 96,30 %**. Cobertura de ramas: 74,93 %.
- JaCoCo genera HTML/XML y `jacocoTestCoverageVerification` falla si la línea baja de 80 %.
- Reporte: `Notix/app/build/reports/jacoco/jacocoTestReport/html/index.html`.
- Pendiente: pruebas de UI/instrumentadas.
- Comando DAO: `./gradlew :app:connectedDebugAndroidTest`.

### 7.8 Notificaciones locales

- `NotificacionUtil` crea el canal “Avisos de trámites” y usa `NotificationCompat`.
- En Android 13+ se solicita `POST_NOTIFICATIONS` desde Mis Trámites.
- `TramiteRepository.refrescarAvisos` devuelve los avisos nuevos no leídos.
- `MisTramitesViewModel` y `AvisosViewModel` notifican esos avisos.
- Se deduplican IDs en `SharedPreferences` (`notix_notificaciones`).
- El toque abre `MainActivity`.
- Funciona cuando la app sincroniza; para avisos con la app cerrada se necesitaría WorkManager o FCM.
- Verificado en dispositivo: un `PUT` de devolución generó el aviso y la notificación del sistema.

---

## 8. Modelo de datos de dominio

```text
Usuario
 ├── id, tipoCuenta, nombreCompleto, correo, contrasena
 ├── run, numeroDocumento, telefono, comuna
 ├── empresa: DatosEmpresa? (razón social, RUT, giro)
 └── numeroNotaria: Int?

Tramite
 ├── id, formato: FormatoTramite, solicitanteId, solicitanteNombre
 ├── actuaEnNombreDe: String?
 ├── tipoFirma: TipoFirma
 ├── estado: EstadoTramite
 ├── documentos: List<Documento>
 ├── antecedentes: Antecedentes
 ├── observacion: String?
 ├── legibilidadConfirmada: Boolean
 ├── firmaEvidencia: String?
 └── historial: List<EventoHistorial>

Aviso
 ├── id, destinatarioId, tramiteId
 ├── mensaje, fechaHora
 └── leido: Boolean
```

Estados:

- `ENVIADO`: visible para la persona como “Enviado”; para el notario como “Pendiente”.
- `DEVUELTO`: “Observado” / “Devuelto”.
- `FINALIZADO`: “Finalizado”.

---

## 9. Cómo ejecutar

### Requisitos

- Android Studio reciente con soporte para AGP 9.2.1.
- JDK 11 o superior para la app; JDK 21 para el backend.
- SDK Android 37 instalado.
- Emulador o dispositivo Android 7.0+ (API 24+).

### Pasos

```bash
cd Notix
./gradlew assembleDebug
```

> En este entorno `gradlew` no tenía permiso de ejecución. Si ocurre, usar `sh gradlew ...` o `chmod +x gradlew`.

También se puede abrir la carpeta `Notix/` directamente en Android Studio y ejecutar la configuración `app`.

### Backend

```bash
cd backend
./mvnw test
./mvnw spring-boot:run -pl ms-usuarios    # puerto 8081
./mvnw spring-boot:run -pl ms-tramites    # puerto 8082
```

No requiere Maven instalado: el wrapper descarga Maven 3.10.1. Las bases H2 quedan en `backend/ms-usuarios/data/` y `backend/ms-tramites/data/`; para reiniciar la demo se detienen los servicios y se borran esas carpetas.

Para el flujo completo, los servicios deben estar levantados antes de usar la app. En el emulador Android las URLs `10.0.2.2:8081` y `10.0.2.2:8082` apuntan al PC. En un dispositivo físico por USB hay que ejecutar `adb reverse tcp:8081 tcp:8081` y `adb reverse tcp:8082 tcp:8082`; `ApiConfig.kt` usa `127.0.0.1` automáticamente.

Guía detallada con comandos, conexión de dispositivos y flujo de prueba completo: [`Guia_ejecucion_y_pruebas.md`](Guia_ejecucion_y_pruebas.md).

Referencia rápida de comandos: [`Comandos_utiles.md`](Comandos_utiles.md).

### Credenciales demo

```
Persona:  ana.demo@notariax.test / demo123
Notario:  notario.demo@notariax.test / notario123
```

También se puede crear una cuenta nueva desde “Crear cuenta”.

---

## 10. Pendientes y deuda técnica

Ordenados por impacto para la rúbrica y el flujo principal.

### Alta prioridad

1. **Pruebas de UI/instrumentadas**
   - Agregar pruebas de login, creación, aprobación y eliminación con Compose UI Test.

2. **Persistencia real del documento adjunto**
   - Hoy solo se guarda el nombre.
   - Copiar el archivo a almacenamiento interno o guardar URI con permiso persistente.
   - Validar tipo/tamaño y evitar rutas nulas.

### Media prioridad

3. **Notificaciones en segundo plano**
   - Las notificaciones locales ya funcionan cuando la app sincroniza.
   - Para avisos con la app cerrada falta WorkManager o FCM.
   - FCM requeriría backend desplegado y credenciales de Firebase.

4. **Navegación y sesión**
   - La barra inferior ya navega entre Trámites, Avisos y Perfil; se eliminó el “Inicio” sin pantalla.
   - Falta manejar el botón atrás del sistema y persistir la sesión (DataStore).
   - El logout está conectado para Persona, Empresa y Notario, y también desde Perfil.

5. **Avisos navegables**
   - `Aviso.tramiteId` no se usa para abrir el trámite.
   - Al tocar un aviso conviene marcar leído y navegar al detalle.

6. **Asignación de trámites al notario**
   - El notario ve todos los trámites de la base.
   - En un escenario real debería ver solo los de su notaría o los asignados.

7. **Validaciones centralizadas**
   - Hoy hay reglas en pantallas, ViewModels y repositorio.
   - Extraer un `TramiteValidator` (como propone la guía) para evitar duplicación.

### Baja prioridad / calidad

8. **Migraciones Room**
   - Ya existe `MIGRATION_2_3` explícita para conservar datos.
   - Activar `exportSchema = true` y versionar el esquema.
   - Evaluar retirar `fallbackToDestructiveMigration()` cuando Room deje de ser prescindible.

9. **Seguridad**
   - Contraseñas en texto plano; para el MVP es aceptable, pero documentar que no es producción.
   - No hay cifrado de base de datos ni control de acceso a nivel de datos.
   - Revisar `allowBackup`.

10. **Fechas**
   - Se usa la fecha real del dispositivo, no una fecha simulada controlada.
   - El orden de trámites se hace por `id` descendente, no por fecha de creación.
   - Considerar `fechaCreacion` y ordenar por ella.

11. **IDs**
    - El backend ya genera IDs secuenciales `TX-2026-XXXX`; la app dejó de generarlos localmente.
    - Mantener esa responsabilidad en el backend.

12. **Documentación y código**
    - `docs/implementacion/Guia_implementacion_Notix.md` describe un estado anterior (repository vacío).
    - Actualizar esa guía o marcarla como histórica.
    - `README.md` debe mantenerse como entrada principal.
    - Revisar nombres de archivo: `AuthViewmodel.kt` vs `AuthViewModel`, `TramitesViewmodel.kt`.

13. **Pruebas**
    - ✅ Pruebas unitarias de `RutValidator`, repositorios, mappers, fechas y ViewModels (77 en total).
    - ✅ Cobertura de línea 96,30 % con JaCoCo y umbral mínimo de 80 %.
    - Pendiente: pruebas de DAO/instrumentadas y pruebas de UI para login, creación y revisión.

14. **Limpieza**
    - Eliminar `.idea/`, `build/` y archivos generados del control de versiones si no son necesarios.
    - Revisar `.gitignore` en raíz y en `Notix/`.

---

## 11. Checklist de pruebas manuales

- [ ] Registro Persona con RUT válido e inválido.
- [ ] Registro Empresa y Notario con campos específicos.
- [ ] Login con correo/clave correctos e incorrectos.
- [ ] Login con tipo de cuenta incorrecto.
- [ ] Crear trámite sin nombre, RUN inválido, correo inválido o comuna vacía.
- [ ] Crear trámite con y sin documento adjunto.
- [ ] Confirmar que el botón de envío exige la declaración de legibilidad.
- [ ] Completar la firma simulada correspondiente y verificar que no se puede enviar sin ella.
- [ ] Aprobar sin marcar legibilidad y verificar que el botón está bloqueado.
- [ ] Intentar aprobar o devolver un trámite ya finalizado o devuelto.
- [ ] Corregir un trámite devuelto con documento observado y reenviarlo; verificar estado `ENVIADO` e historial.
- [ ] Verificar que el documento observado exige reemplazo antes de reenviar.
- [ ] Cerrar sesión como Persona, Empresa y Notario; verificar el regreso a Welcome.
- [ ] Con ambos microservicios activos, iniciar sesión y comprobar que el usuario queda en Room.
- [ ] Crear un trámite y verificar que el ID lo asigna el backend (`TX-2026-XXXX`).
- [ ] Aprobar o devolver desde la app y comprobar el nuevo estado en la base H2 y en la app.
- [ ] Verificar la tarjeta del próximo feriado en el catálogo; con Nager.Date caído debe mostrar el respaldo.
- [ ] Detener el backend y confirmar que la app conserva la última lista local y muestra error al escribir.
- [ ] Ver el trámite creado en “Mis trámites”.
- [ ] Ver el trámite en la bandeja del notario.
- [ ] Aprobar un trámite y verificar estado, historial y aviso.
- [ ] Intentar devolver sin observación y verificar que se bloquea.
- [ ] Devolver con observación y verificar el aviso.
- [ ] Ver la observación en el detalle de la persona.
- [ ] Marcar un aviso como leído.
- [ ] Cerrar y abrir la app y verificar que los datos siguen.
- [ ] Verificar que la sesión no se mantiene al reiniciar.
- [ ] Verificar que el archivo adjunto no se conserva tras reiniciar (comportamiento actual).

---

## 12. Verificación del build

Comando usado:

```bash
cd Notix
JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:assembleDebug
```

Resultado: **BUILD SUCCESSFUL** con el entorno local (AGP 9.2.1, Kotlin 2.2.10, Room 2.8.5).

Pruebas y cobertura:

```bash
cd Notix
JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk ./gradlew \
  :app:testDebugUnitTest :app:jacocoTestReport :app:jacocoTestCoverageVerification
```

- **82 pruebas JVM**, sin fallos.
- **96,30 % de cobertura de línea**.
- **74,93 % de cobertura de ramas**.
- **7 pruebas DAO instrumentadas**, sin fallos.
- Reporte HTML: `Notix/app/build/reports/jacoco/jacocoTestReport/html/index.html`.
- `jacocoTestCoverageVerification` exige al menos 80 % de línea.

Pruebas DAO:

```bash
cd Notix
JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk ./gradlew :app:connectedDebugAndroidTest
```

Verifican Room en memoria: inserciones, transacciones de estado/reenvío, reemplazo sin duplicados, borrado en cascada, avisos y correo duplicado.

Observaciones del entorno:

- `gradlew` no tenía permiso de ejecución; se ejecutó `chmod +x gradlew`.
- `JAVA_HOME` apuntaba a un directorio inválido (`/usr/lib/jvm/java-21-openjdk`); se usó el JDK Temurin 21 disponible mediante variable de entorno.
- Gradle advierte que AGP 9.2.1 fue probado hasta compileSdk 37.0 y el proyecto usa 37.1. No impide compilar, pero conviene alinear versiones.
- Room advierte que `fallbackToDestructiveMigration()` está deprecado; la migración 2 → 3 sí está definida y conserva datos.

### Backend

Comando usado:

```bash
cd backend
JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk ./mvnw test
```

Resultado: **BUILD SUCCESSFUL**, 11 pruebas ejecutadas (contexto de ambos servicios, registro, login, creación, aprobación, devolución, reenvío, eliminación y avisos), sin fallos.

También se levantaron ambos JAR y se verificó con `curl`:

- `GET /api/usuarios` devuelve los dos usuarios demo.
- `POST /api/usuarios/login` devuelve `200` con credenciales correctas y `403` con tipo incorrecto.
- `GET /api/tramites` devuelve los cuatro trámites demo.
- `POST /api/tramites` crea `TX-2026-0043` en estado `ENVIADO`.
- Aprobar sin legibilidad devuelve `409`; con legibilidad devuelve `200` y `FINALIZADO`.
- `GET /api/avisos/USR-001` devuelve los avisos generados.

La integración desde la app se verificó con una prueba JVM temporal de contrato contra estos servicios: login, listado de trámites, documentos observados, historial y avisos. La prueba se retiró después para no depender de servicios activos.

### App Android con Retrofit

Comando ejecutado:

```bash
cd Notix
JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:assembleDebug
```

Resultado: **BUILD SUCCESSFUL**. Compila con Retrofit 2.11, OkHttp 4.12, Gson y el permiso `INTERNET`. La suite actual tiene 82 pruebas JVM y 96,30 % de cobertura de línea.

El emulador local no arrancó (terminó con `SIGSEGV`), pero la integración se verificó en un **dispositivo físico** conectado por USB con `adb reverse` y ambos microservicios activos:

- Login de Persona contra `ms-usuarios`.
- Mis Trámites mostró los datos sincronizados desde `ms-tramites`.
- Creación de `TX-2026-0043` desde la app; la API confirmó el nuevo trámite con su historial inicial.
- Login como notario y aprobación de `TX-2026-0043` con legibilidad marcada; el backend quedó en `FINALIZADO` con historial de `NOTARIO`.
- Eliminación de `TX-2026-0042` desde la UI: desapareció del backend y de la lista local; también se borraron sus avisos.
- Una devolución creada desde el backend generó un aviso nuevo; al abrir Avisos, la app mostró la notificación local en el sistema y guardó el ID para no repetirla.
- La tarjeta de feriados mostró “Inmaculada Concepción · 08/12/2026” desde Nager.Date.
- Sin backend activo, el login local y la caché de Room siguieron funcionando; las escrituras quedaron pendientes de conexión.

También se ejecutó una prueba JVM temporal contra los servicios reales: login, listado de trámites, documentos observados, historial y avisos. Pasó correctamente y luego se retiró para no dejar una prueba dependiente del backend activo.

---

## 13. Recomendación de siguiente etapa

Estado tras las Fases 1, 2, 3 y 4 de la EP4 (23-11-2026): flujos de la app, microservicios, integración Retrofit, API externa, DELETE desde la UI y pruebas JVM/DAO ya están implementados. Para cerrar la EP4 queda:

1. Guardar el **documento adjunto** de forma persistente (hoy solo metadatos).
2. Agregar **notificaciones en segundo plano** con WorkManager o FCM.
3. Completar el **botón atrás** y la sesión persistente (el perfil y el logout ya están conectados).
4. Agregar **pruebas de UI/instrumentadas**; las 82 JVM y 7 DAO no cubren la interfaz.
5. Verificar el **APK firmado** y completar README/Trello/evidencias de entrega.

---

## 14. Decisiones y notas

- Se priorizó un flujo completo Persona/Empresa ↔ Notario con persistencia local real.
- La autenticación es local y con contraseña en texto plano por tratarse de un MVP académico.
- Los documentos se representan por metadatos; no se almacena contenido real.
- La biometría, e-token y firma avanzada son simulaciones.
- El backend es la fuente de verdad para escrituras; Room actúa como caché local y permite mostrar la última información si no hay red.
- Las lecturas de trámites y avisos refrescan Room desde el backend al entrar a las pantallas; la UI observa `Flow` de Room.
- Las escrituras (crear, aprobar, devolver, reenviar) llaman primero al backend y luego actualizan Room con la respuesta.
- El login admite fallback local solo cuando falla la red y el usuario ya existe en la caché.
- La API externa (Nager.Date) es opcional: `runCatching` evita que un fallo bloquee el catálogo o la creación.
- El backend usa H2 en archivo para evitar depender de un motor externo en la demo.
- Las contraseñas del backend también quedan en texto plano por el alcance académico; no es una implementación de producción.
- Se habilita tráfico HTTP local (`usesCleartextTraffic`) solo para la demo con `10.0.2.2`; en producción debe usarse HTTPS.
- El prototipo HTML se conserva como referencia, no como parte funcional de la entrega Android.

---

## 15. Glosario rápido

| Término | Significado |
|---|---|
| DAO | Interfaz de acceso a datos de Room |
| Entidad | Tabla de la base de datos |
| Mapper | Conversión entre entidad y modelo de dominio |
| Seeder | Carga inicial de datos ficticios |
| Flow | Stream reactivo de datos |
| StateFlow | Estado observable para la UI |
| KSP | Procesador de símbolos usado por Room |
| MVP | Producto mínimo viable académico |
